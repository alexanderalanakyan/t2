package none.inferno.utils

import com.mojang.blaze3d.buffers.GpuBuffer
import com.mojang.blaze3d.buffers.GpuBufferSlice
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.systems.CommandEncoder
import com.mojang.blaze3d.systems.RenderPass
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.BufferBuilder
import com.mojang.blaze3d.vertex.ByteBufferBuilder
import com.mojang.blaze3d.vertex.MeshData
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexFormat
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.MappableRingBuffer
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.ShapeRenderer
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import none.inferno.references.BasicReferences
import none.inferno.references.Pipelines
import org.joml.Vector3f
import org.joml.Vector4f
import org.lwjgl.system.MemoryUtil
import java.util.OptionalDouble
import java.util.OptionalInt

class Render {
    /*
    Just a class following the tutorial for rendering with a few tweaks in 1.21.10 (as of 12/15/25)
     */
    data class Waypoint(
        val x: Double,
        val y: Double,
        val z: Double,
        val size: Double = 0.5
    ) {
        fun toAABB(): AABB {
            val h = size / 2.0
            return AABB(x - h, y - h, z - h, x + h, y + h, z + h)
        }
    }

    sealed class BoxTarget {
        data class Entities(val entities: List<Entity>) : BoxTarget()
        data class Waypoints(val points: List<Waypoint>) : BoxTarget()
    }

    private val allocator : ByteBufferBuilder = ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE)
    private var buffer : BufferBuilder? = null

    private fun pushToRenderMatrix(
        context: WorldRenderContext,
        pipeline: RenderPipeline,
        target: BoxTarget,
        wireframe: Boolean,
        r: Float,
        g: Float,
        b: Float,
        a: Float
    ) {
        val matrices: PoseStack = context.matrices()
        val camera: Vec3 = context.worldState().cameraRenderState.pos
        val tickDelta = context.gameRenderer().mainCamera.partialTickTime

        matrices.pushPose()
        matrices.translate(-camera.x, -camera.y, -camera.z)

        // Fresh buffer per render call
        buffer = BufferBuilder(allocator, pipeline.vertexFormatMode, pipeline.vertexFormat)

        when (target) {
            is BoxTarget.Entities -> {
                for (entity in target.entities) {
                    if (entity == Minecraft.getInstance().player) continue

                    val ix = entity.xOld + (entity.x - entity.xOld) * tickDelta
                    val iy = entity.yOld + (entity.y - entity.yOld) * tickDelta
                    val iz = entity.zOld + (entity.z - entity.zOld) * tickDelta

                    val boxAabb = entity.boundingBox
                        .move(ix - entity.x, iy - entity.y, iz - entity.z)
                        .inflate(0.02)

                    if (isValidAABB(boxAabb)) {
                        renderBox(matrices, buffer!!, boxAabb, wireframe, r, g, b, a)
                    } else {
                        continue
                    }
                }
            }

            is BoxTarget.Waypoints -> {

                for (wp in target.points) {
                    val boxAabb = wp.toAABB()
                    if (isValidAABB(boxAabb)) {
                        renderBox(matrices, buffer!!, boxAabb, wireframe, r, g, b, a)
                    } else {
                        continue
                    }
                }
            }
        }

        //println("Vertices in buffer after push: ${buffer.toString()}")
        matrices.popPose()
    }

    private fun renderBox(
        matrices: PoseStack,
        buffer: BufferBuilder,
        box: AABB,
        wireframe: Boolean,
        r: Float,
        g: Float,
        b: Float,
        a: Float
    ) {
        if (!isValidAABB(box)) return

        if (wireframe) {
            ShapeRenderer.renderLineBox(matrices.last(), buffer, box, r, g, b, a)
        } else {
            ShapeRenderer.addChainedFilledBoxVertices(
                matrices,
                buffer,
                box.minX, box.minY, box.minZ,
                box.maxX, box.maxY, box.maxZ,
                r, g, b, a
            )
        }


    }

    // Helper to validate AABBs
    private fun isValidAABB(aabb: AABB): Boolean {
        return !aabb.hasNaN() &&
                (aabb.minX != aabb.maxX &&
                        aabb.minY != aabb.maxY &&
                        aabb.minZ != aabb.maxZ)
    }

    private val COLOR_MODULATOR : Vector4f = Vector4f(1f, 1f, 1f, 1f)
    private var vertexBuffer : MappableRingBuffer? = null

    fun extractAndDrawBox(
        context : WorldRenderContext,
        culling: Boolean,
        wireframe : Boolean,
        target: BoxTarget,
        r : Float,
        g : Float,
        b : Float,
        a : Float
    ) {

        val pipeline = when {
            wireframe && culling -> Pipelines.linesPipeline
            wireframe && !culling -> Pipelines.unculledLinesPipeline
            !wireframe && culling -> Pipelines.boxPipeline
            else -> Pipelines.unculledLinesPipeline
        }

        pushToRenderMatrix(context, pipeline, target, wireframe, r, g, b, a)
        drawFilledBoxThroughWalls(Minecraft.getInstance(), pipeline)
    }

    private fun drawFilledBoxThroughWalls(
        client : Minecraft,
        pipeline : RenderPipeline
    ) {
        val builtBuffer: MeshData = try
        {
            buffer?.buildOrThrow() ?: return
        }

        catch (e: IllegalStateException)
        {
            //skip render if no buffer data (aka no waypoints/entities)
            return
        }

        val drawParameters : MeshData.DrawState = builtBuffer.drawState()
        val format : VertexFormat = drawParameters.format

        val vertices : GpuBuffer = upload(drawParameters, format, builtBuffer)

        draw(client, pipeline, builtBuffer, drawParameters, format, vertices)

        vertexBuffer?.rotate()
        buffer = null
    }

    private fun upload(
        drawParameters : MeshData.DrawState,
        format : VertexFormat,
        builtBuffer : MeshData
    ) : GpuBuffer {
        val vertexBufferSize = drawParameters.vertexCount * format.vertexSize

        if(vertexBuffer == null || vertexBuffer!!.size() < vertexBufferSize) {

            vertexBuffer = MappableRingBuffer(
                {
                    "${BasicReferences.modid} render pipeline"
                },
                GpuBuffer.USAGE_VERTEX or GpuBuffer.USAGE_MAP_WRITE,
                vertexBufferSize
            )

        }
        val commandEncoder : CommandEncoder = RenderSystem.getDevice().createCommandEncoder()

        val mappedView : GpuBuffer.MappedView = commandEncoder.mapBuffer(
            vertexBuffer?.currentBuffer()?.slice(0,
                builtBuffer.vertexBuffer().remaining()
            ), false, true)

        MemoryUtil.memCopy(builtBuffer.vertexBuffer(), mappedView.data())
        return vertexBuffer?.currentBuffer() ?: error("No vertex buffer")

    }

    private fun draw(
        client : Minecraft,
        pipeline : RenderPipeline,
        builtBuffer: MeshData,
        drawParameters : MeshData.DrawState,
        format : VertexFormat,
        vertices : GpuBuffer
    ) {

        var indices : GpuBuffer?
        var indexType : VertexFormat.IndexType?

        if(pipeline.vertexFormatMode == VertexFormat.Mode.QUADS) {

            builtBuffer.sortQuads(allocator, RenderSystem.getProjectionType().vertexSorting())
            indices = pipeline.vertexFormat.uploadImmediateIndexBuffer(builtBuffer.indexBuffer())
            indexType = builtBuffer.drawState().indexType()

        } else {

            val shapeIndexBuffer : RenderSystem.AutoStorageIndexBuffer = RenderSystem.getSequentialBuffer(pipeline.vertexFormatMode)
            indices = shapeIndexBuffer.getBuffer(drawParameters.indexCount())
            indexType = shapeIndexBuffer.type()

        }

        val dynamicTransforms : GpuBufferSlice = RenderSystem.getDynamicUniforms()
            .writeTransform(
                RenderSystem.getModelViewMatrix(),
                COLOR_MODULATOR,
                Vector3f(),
                RenderSystem.getTextureMatrix(),
                1f
            )

        val renderPass : RenderPass = RenderSystem.getDevice()
            .createCommandEncoder()
            .createRenderPass(
                { "${BasicReferences.modid} render pipeline" },
                client.mainRenderTarget.colorTextureView,
                OptionalInt.empty(),
                client.mainRenderTarget.depthTextureView,
                OptionalDouble.empty()
            )

        renderPass.setPipeline(pipeline)
        RenderSystem.bindDefaultUniforms(renderPass)
        renderPass.setUniform("DynamicTransforms", dynamicTransforms)
        renderPass.setVertexBuffer(0, vertices)
        renderPass.setIndexBuffer(indices, indexType)
        renderPass.drawIndexed(0, 0, drawParameters.indexCount, 1)
        renderPass.close()

        builtBuffer.close()
    }

    fun close() {
        allocator.close()
        if(vertexBuffer != null) vertexBuffer?.close(); vertexBuffer = null
    }
}