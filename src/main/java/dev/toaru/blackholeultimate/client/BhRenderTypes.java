package dev.toaru.blackholeultimate.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;

/** Custom render types for Black Hole Ultimate visual effects. */
public final class BhRenderTypes extends RenderType {
    private BhRenderTypes(String name, VertexFormat format, VertexFormat.Mode mode, int size, boolean crumbling, boolean sort,
                          Runnable setup, Runnable clear) {
        super(name, format, mode, size, crumbling, sort, setup, clear);
        throw new IllegalStateException("not meant to be constructed");
    }

    /** Dark/opaque geometry - black hole cores, dome surfaces */
    public static final RenderType DARK = create("blackholeultimate:dark", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 8192, false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .createCompositeState(false));

    /** Additive glow geometry - rings, lensing, beams, shockwaves */
    public static final RenderType GLOW = RenderType.lightning();

    /** Distortion effect - glass-like domes with texture */
    public static final RenderType DISTORTION = create("blackholeultimate:distortion", DefaultVertexFormat.POSITION_TEX_COLOR, VertexFormat.Mode.QUADS, 8192, false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_TEX_COLOR_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(
                            net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("blackholeultimate", "textures/misc/distortion.png"), false, false))
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .createCompositeState(false));

    /** Beam effect - quasar beam */
    public static final RenderType BEAM = create("blackholeultimate:beam", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 8192, false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .setTransparencyState(RenderStateShard.ADDITIVE_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .createCompositeState(false));

    /** Wave ripple - gravitational wave rings */
    public static final RenderType WAVE = create("blackholeultimate:wave", DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.QUADS, 8192, false, true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.POSITION_COLOR_SHADER)
                    .setTransparencyState(RenderStateShard.ADDITIVE_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .createCompositeState(false));

    private static RenderType create(String name, VertexFormat format, VertexFormat.Mode mode, int size, boolean crumbling, boolean sort, RenderType.CompositeState state) {
        return new RenderType(name, format, mode, size, crumbling, sort, () -> {}, () -> {}) {
            @Override
            public RenderType.CompositeState state() { return state; }
        };
    }
}