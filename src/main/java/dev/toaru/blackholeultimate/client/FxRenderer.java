package dev.toaru.blackholeultimate.client;

import dev.toaru.blackholeultimate.BlackHoleUltimateMod;
import dev.toaru.blackholeultimate.client.ClientFx.Effect;
import dev.toaru.blackholeultimate.net.FxType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

/**
 * Renders all black hole variants as real 3D geometry.
 * Each variant has unique visuals: micro (small/fast), classic (balanced), quasar (beam), void (devouring), event horizon (protective), gravitational wave (ripples).
 */
@Mod.EventBusSubscriber(modid = BlackHoleUltimateMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class FxRenderer {
    private FxRenderer() { }

    private static final double MAX_DISTANCE = 220.0;

    @SubscribeEvent
    public static void onRender(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS || ClientFx.ACTIVE.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        float partial = mc.isPaused() ? 0f : event.getPartialTick();
        long now = mc.level.getGameTime();
        Vec3 cam = event.getCamera().getPosition();
        Matrix4f pose = event.getPoseStack().last().pose();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();

        // Pass 1: Dark geometry (cores, domes)
        Geo dark = new Geo(buffers.getBuffer(BhRenderTypes.DARK), pose, cam);
        for (Effect fx : ClientFx.ACTIVE.values()) {
            if (fx.pos.distanceTo(cam) <= MAX_DISTANCE) drawDark(fx, fx.age(now, partial), dark, cam);
        }
        buffers.endBatch(BhRenderTypes.DARK);

        // Pass 2: Distortion (glass-like domes)
        Geo distortion = new Geo(buffers.getBuffer(BhRenderTypes.DISTORTION), pose, cam);
        for (Effect fx : ClientFx.ACTIVE.values()) {
            if (fx.pos.distanceTo(cam) <= MAX_DISTANCE) drawDistortion(fx, fx.age(now, partial), distortion, cam);
        }
        buffers.endBatch(BhRenderTypes.DISTORTION);

        // Pass 3: Glow (rings, lensing, shockwaves)
        Geo glow = new Geo(buffers.getBuffer(BhRenderTypes.GLOW), pose, cam);
        for (Effect fx : ClientFx.ACTIVE.values()) {
            if (fx.pos.distanceTo(cam) <= MAX_DISTANCE) drawGlow(fx, fx.age(now, partial), glow, cam);
        }
        buffers.endBatch(BhRenderTypes.GLOW);

        // Pass 4: Beams (quasar)
        Geo beam = new Geo(buffers.getBuffer(BhRenderTypes.BEAM), pose, cam);
        for (Effect fx : ClientFx.ACTIVE.values()) {
            if (fx.pos.distanceTo(cam) <= MAX_DISTANCE) drawBeam(fx, fx.age(now, partial), beam, cam);
        }
        buffers.endBatch(BhRenderTypes.BEAM);

        // Pass 5: Wave ripples (gravitational wave)
        Geo wave = new Geo(buffers.getBuffer(BhRenderTypes.WAVE), pose, cam);
        for (Effect fx : ClientFx.ACTIVE.values()) {
            if (fx.pos.distanceTo(cam) <= MAX_DISTANCE) drawWave(fx, fx.age(now, partial), wave, cam);
        }
        buffers.endBatch(BhRenderTypes.WAVE);
    }

    private static double clamp01(double v) { return v < 0 ? 0 : (v > 1 ? 1 : v); }
    private static double smoothstep(double t) { return t * t * (3.0 - 2.0 * t); }
    private static double lerp(double a, double b, double t) { return a + (b - a) * t; }

    private static int segments(Effect fx, Vec3 cam) {
        double dist = fx.pos.distanceTo(cam);
        return dist < 50 ? 56 : (dist < 120 ? 36 : 24);
    }

    /** Current radius based on phase. */
    private static double currentRadius(Effect fx, double age) {
        double form = clamp01(age / Math.max(1.0, fx.aux.y));
        double total = fx.duration;
        double activeStart = fx.aux.y;
        double activeEnd = activeStart + (total - activeStart) * 0.8;
        double collapseStart = activeEnd;

        if (age < activeStart) {
            return fx.aux.x * smoothstep(form);
        } else if (age < activeEnd) {
            double pulse = Math.sin((age - activeStart) * 0.15) * 0.05 + 1.0;
            return fx.aux.x * pulse;
        } else if (age < total) {
            double collapseProgress = clamp01((age - collapseStart) / Math.max(1, total - collapseStart));
            double ease = 1.0 - smoothstep(collapseProgress);
            return fx.aux.x * ease * ease;
        }
        return 0;
    }

    /** Draw dark/opaque geometry. */
    private static void drawDark(Effect fx, double age, Geo g, Vec3 cam) {
        int seg = segments(fx, cam);
        Vec3 c = fx.pos;
        double r = currentRadius(fx, age);
        if (r < 0.02) return;

        switch (fx.type) {
            case MICRO_BLACK_HOLE, CLASSIC_BLACK_HOLE, QUASAR_BLACK_HOLE, VOID_BLACK_HOLE -> {
                // Core sphere during formation
                if (age < fx.aux.y) {
                    double coreR = 0.25 * clamp01(age / Math.max(1.0, fx.aux.y * 0.5));
                    if (coreR > 0.02) {
                        ColorScheme cs = getColorScheme(fx.type);
                        g.color(cs.coreR, cs.coreG, cs.coreB, 0.5f).sphere(c.add(0, 1.5, 0), coreR, 8, seg / 2);
                    }
                }
                // Main dome/sphere
                g.color(0.02f, 0.02f, 0.08f, 0.12f).sphere(c, r * 1.05, 12, seg);
                g.color(0.0f, 0.0f, 0.03f, 0.25f).sphere(c, r, 16, seg);
            }
            case EVENT_HORIZON -> {
                // Protective dome - more transparent, blue-tinted
                g.color(0.1f, 0.3f, 0.8f, 0.08f).sphere(c, r * 1.02, 12, seg);
                g.color(0.05f, 0.2f, 0.6f, 0.15f).sphere(c, r, 16, seg);
            }
            case GRAVITATIONAL_WAVE -> {
                // No persistent dark geometry for waves
            }
            case MICRO_BLACK_HOLE_COLLAPSE, CLASSIC_BLACK_HOLE_COLLAPSE, QUASAR_BLACK_HOLE_COLLAPSE, VOID_BLACK_HOLE_COLLAPSE -> {
                // Collapse handled in glow
            }
        }
    }

    /** Draw distortion effect. */
    private static void drawDistortion(Effect fx, double age, Geo g, Vec3 cam) {
        int seg = segments(fx, cam);
        Vec3 c = fx.pos;
        double r = currentRadius(fx, age);
        if (r < 0.1) return;

        double form = clamp01(age / Math.max(1.0, fx.aux.y));
        if (form < 1.0) return;

        double activeStart = fx.aux.y;
        double activeEnd = activeStart + (fx.duration - activeStart) * 0.8;
        if (age < activeStart || age > activeEnd) return;

        double distortionAlpha = 0.15 + 0.1 * Math.sin(age * 0.1);
        ColorScheme cs = getColorScheme(fx.type);
        g.color(cs.distortR, cs.distortG, cs.distortB, (float) distortionAlpha).sphere(c, r * 1.01, 12, seg);
    }

    /** Draw glow geometry. */
    private static void drawGlow(Effect fx, double age, Geo g, Vec3 cam) {
        int seg = segments(fx, cam);
        Vec3 c = fx.pos;
        double r = currentRadius(fx, age);
        if (r < 0.02) return;

        switch (fx.type) {
            case MICRO_BLACK_HOLE -> microBlackHoleGlow(fx, age, g, cam, seg);
            case CLASSIC_BLACK_HOLE -> classicBlackHoleGlow(fx, age, g, cam, seg);
            case QUASAR_BLACK_HOLE -> quasarBlackHoleGlow(fx, age, g, cam, seg);
            case VOID_BLACK_HOLE -> voidBlackHoleGlow(fx, age, g, cam, seg);
            case EVENT_HORIZON -> eventHorizonGlow(fx, age, g, cam, seg);
            case GRAVITATIONAL_WAVE -> gravitationalWaveGlow(fx, age, g, cam, seg);
            case MICRO_BLACK_HOLE_COLLAPSE -> collapseGlow(fx, age, g, cam, seg, getColorScheme(FxType.MICRO_BLACK_HOLE));
            case CLASSIC_BLACK_HOLE_COLLAPSE -> collapseGlow(fx, age, g, cam, seg, getColorScheme(FxType.CLASSIC_BLACK_HOLE));
            case QUASAR_BLACK_HOLE_COLLAPSE -> collapseGlow(fx, age, g, cam, seg, getColorScheme(FxType.QUASAR_BLACK_HOLE));
            case VOID_BLACK_HOLE_COLLAPSE -> collapseGlow(fx, age, g, cam, seg, getColorScheme(FxType.VOID_BLACK_HOLE));
            case EVENT_HORIZON_COLLAPSE -> collapseGlow(fx, age, g, cam, seg, getColorScheme(FxType.EVENT_HORIZON));
            case GRAVITATIONAL_WAVE_COLLAPSE -> collapseGlow(fx, age, g, cam, seg, getColorScheme(FxType.GRAVITATIONAL_WAVE));
        }
    }

    private static ColorScheme getColorScheme(FxType type) {
        return switch (type) {
            case MICRO_BLACK_HOLE, MICRO_BLACK_HOLE_COLLAPSE -> new ColorScheme(0.4f, 0.6f, 1.0f, 0.3f, 0.5f, 1.0f);
            case CLASSIC_BLACK_HOLE, CLASSIC_BLACK_HOLE_COLLAPSE -> new ColorScheme(0.6f, 0.3f, 1.0f, 0.4f, 0.3f, 1.0f);
            case QUASAR_BLACK_HOLE, QUASAR_BLACK_HOLE_COLLAPSE -> new ColorScheme(1.0f, 0.4f, 0.8f, 1.0f, 0.2f, 0.6f);
            case VOID_BLACK_HOLE, VOID_BLACK_HOLE_COLLAPSE -> new ColorScheme(0.3f, 0.0f, 0.5f, 0.2f, 0.0f, 0.4f);
            case EVENT_HORIZON, EVENT_HORIZON_COLLAPSE -> new ColorScheme(0.2f, 0.6f, 1.0f, 0.3f, 0.7f, 1.0f);
            case GRAVITATIONAL_WAVE, GRAVITATIONAL_WAVE_COLLAPSE -> new ColorScheme(0.7f, 0.8f, 1.0f, 0.5f, 0.6f, 1.0f);
            default -> new ColorScheme(0.5f, 0.5f, 1.0f, 0.5f, 0.5f, 1.0f);
        };
    }

    private record ColorScheme(float coreR, float coreG, float coreB, float distortR, float distortG, float distortB) {}

    // ===== Variant-specific glow rendering =====

    private static void microBlackHoleGlow(Effect fx, double age, Geo g, Vec3 cam, int seg) {
        Vec3 c = fx.pos;
        double r = currentRadius(fx, age);
        double form = clamp01(age / Math.max(1.0, fx.aux.y));
        double ease = smoothstep(form);

        if (age < fx.aux.y) {
            // Formation: small core with quick rings
            double coreR = 0.3 * ease;
            Vec3 corePos = c.add(0, 1.5, 0);
            g.color(0.4f, 0.6f, 1.0f, (float) (0.8 * ease)).sphere(corePos, coreR, 8, seg / 2);
            for (int i = 0; i < 2; i++) {
                double ringPhase = age * 0.05 + i * 3.14;
                g.color(0.4f, 0.6f, 1.0f, (float) (0.4 * ease)).ring(corePos, new Vec3(0, 1, 0), coreR * (1.3 + i * 0.3), coreR * 0.05, seg, ringPhase, true);
            }
            return;
        }

        double activeStart = fx.aux.y;
        double activeEnd = activeStart + (fx.duration - activeStart) * 0.8;
        double activeAge = age - activeStart;
        double activeProgress = clamp01(activeAge / Math.max(1, activeEnd - activeStart));

        // Photon ring
        Vec3 toCam = cam.subtract(c).normalize();
        g.color(0.4f, 0.6f, 1.0f, 0.6f).ring(c, toCam, r * 1.01, r * 0.02, seg, 0, false);

        // Rotating rings
        for (int i = 0; i < 3; i++) {
            double phase = activeAge * 0.02 + i * 2.0;
            Vec3 normal = new Vec3(Math.sin(0.5) * Math.cos(i * 2.0), Math.cos(0.5), Math.sin(0.5) * Math.sin(i * 2.0)).normalize();
            g.color(0.3f, 0.5f, 1.0f, (float) (0.3 * (1.0 - i * 0.2))).ring(c, normal, r * (1.03 + i * 0.05), r * 0.015, seg, phase, true);
        }

        // Collapse phase
        if (age >= activeEnd) {
            double cp = clamp01((age - activeEnd) / Math.max(1, fx.duration - activeEnd));
            double implR = r * (1.0 - cp * 0.9);
            float fa = (float) (1.0 - cp);
            if (implR > 0.02) {
                g.color(1.0f, 0.9f, 0.7f, fa * 0.8).sphere(c, implR * 1.3, 8, 16);
                g.color(0.4f, 0.6f, 1.0f, fa * 0.5).sphere(c, implR, 12, seg);
            }
        }
    }

    private static void classicBlackHoleGlow(Effect fx, double age, Geo g, Vec3 cam, int seg) {
        Vec3 c = fx.pos;
        double r = currentRadius(fx, age);
        double form = clamp01(age / Math.max(1.0, fx.aux.y));
        double ease = smoothstep(form);

        if (age < fx.aux.y) {
            double coreR = 0.4 * ease;
            Vec3 corePos = c.add(0, 1.5, 0);
            g.color(0.6f, 0.3f, 1.0f, (float) (0.9 * ease)).sphere(corePos, coreR, 8, seg / 2);
            for (int i = 0; i < 3; i++) {
                double ringPhase = age * 0.03 + i * 2.0;
                double tilt = Math.sin(ringPhase * 0.5) * 0.3;
                Vec3 normal = new Vec3(Math.sin(tilt), Math.cos(tilt), 0).normalize();
                g.color(0.6f, 0.3f, 1.0f, (float) (0.5 * ease * (1.0 - i * 0.2))).ring(corePos, normal, coreR * (1.2 + i * 0.4), coreR * 0.08, seg, ringPhase, false);
            }
            return;
        }

        double activeStart = fx.aux.y;
        double activeEnd = activeStart + (fx.duration - activeStart) * 0.8;
        double activeAge = age - activeStart;
        double activeProgress = clamp01(activeAge / Math.max(1, activeEnd - activeStart));

        // Photon ring + lensing
        Vec3 toCam = cam.subtract(c).normalize();
        g.color(0.6f, 0.3f, 1.0f, (float) (0.7 * (1.0 - activeProgress * 0.3))).ring(c, toCam, r * 1.01, r * 0.02, seg, 0, false);
        g.color(1.0f, 0.7f, 0.3f, (float) (0.3 * (1.0 - activeProgress * 0.3))).ring(c, toCam, r * 1.2, r * 0.1, seg, 0, false);
        g.color(0.6f, 0.8f, 1.0f, (float) (0.2 * (1.0 - activeProgress * 0.3))).ring(c, toCam, r * 2.0, r * 0.03, seg, 0, false);

        // Accretion disk
        Vec3 diskNormal = new Vec3(Math.sin(0.35) * Math.cos(0.6), Math.cos(0.35), Math.sin(0.35) * Math.sin(0.6)).normalize();
        for (int k = 0; k < 12; k++) {
            double u = k / 11.0;
            double radius = r * (1.5 + 3.0 * u);
            double omega = 0.15 / Math.pow(1.0 + 2.5 * u, 1.5);
            g.color(1.0f, (float) lerp(0.9, 0.3, u), (float) lerp(0.8, 0.0, u), (float) ((0.8 - 0.5 * u) * (1.0 - activeProgress * 0.2)))
                    .ring(c, diskNormal, radius, r * (0.04 + 0.06 * u), seg, activeAge * omega + k * 0.6, true);
        }

        // Infall streams
        for (int arm = 0; arm < 6; arm++) {
            double base = arm * Math.PI * 2.0 / 6;
            Vec3 prev = null;
            for (int j = 0; j <= 15; j++) {
                double s = j / 15.0;
                double rad = r * 1.2 + (fx.aux.x * 0.5 - r * 1.2) * Math.pow(1.0 - s, 1.6);
                double ang = base + age * 0.04 + s * 4.0;
                double h = Math.sin(base * 3.0 + s * 3.0) * (1.0 - s) * fx.aux.x * 0.08;
                Vec3 pt = c.add(Math.cos(ang) * rad, h, Math.sin(ang) * rad);
                if (prev != null) {
                    g.color(1.0f, (float) lerp(0.5, 0.9, s), (float) lerp(0.1, 0.8, s), (float) (0.4 * (1.0 - activeProgress * 0.3) * (0.25 + 0.75 * s)));
                    g.ribbon(prev, pt, 0.02 + 0.03 * s);
                }
                prev = pt;
            }
        }

        // Gravitational waves
        for (int w = 0; w < 3; w++) {
            double ph = ((age * 0.8 + w * 40.0) % 120.0) / 120.0;
            g.color(0.5f, 0.6f, 1.0f, (float) ((1.0 - ph) * 0.25 * (1.0 - activeProgress * 0.3)))
                    .ring(c, new Vec3(0, 1, 0), ph * fx.aux.x, 0.05 + 0.1 * ph, seg, 0, false);
        }

        // Collapse
        if (age >= activeEnd) {
            double cp = clamp01((age - activeEnd) / Math.max(1, fx.duration - activeEnd));
            double implR = r * (1.0 - cp * 0.9);
            float fa = (float) (1.0 - cp);
            if (implR > 0.02) {
                g.color(1.0f, 0.95f, 0.85f, fa * 0.9).sphere(c, implR * 1.5, 8, 16);
                g.color(0.5f, 0.3f, 1.0f, fa * 0.6).sphere(c, implR, 12, seg);
                for (int i = 0; i < 3; i++) {
                    double ringR = implR + cp * r * (0.5 + i * 0.3);
                    g.color(0.6f, 0.4f, 1.0f, (float) ((1.0 - cp) * (0.4 - i * 0.1))).ring(c, new Vec3(0, 1, 0), ringR, r * 0.05, seg, 0, false);
                }
            }
        }
    }

    private static void quasarBlackHoleGlow(Effect fx, double age, Geo g, Vec3 cam, int seg) {
        Vec3 c = fx.pos;
        double r = currentRadius(fx, age);
        double form = clamp01(age / Math.max(1.0, fx.aux.y));
        double ease = smoothstep(form);

        if (age < fx.aux.y) {
            double coreR = 0.5 * ease;
            Vec3 corePos = c.add(0, 1.5, 0);
            g.color(1.0f, 0.4f, 0.8f, (float) (0.9 * ease)).sphere(corePos, coreR, 8, seg / 2);
            for (int i = 0; i < 4; i++) {
                double ringPhase = age * 0.04 + i * 1.5;
                Vec3 normal = new Vec3(Math.sin(0.4 + i * 0.2) * Math.cos(i), Math.cos(0.4 + i * 0.2), Math.sin(0.4 + i * 0.2) * Math.sin(i)).normalize();
                g.color(1.0f, 0.3f, 0.7f, (float) (0.6 * ease * (1.0 - i * 0.15))).ring(corePos, normal, coreR * (1.1 + i * 0.3), coreR * 0.06, seg, ringPhase, true);
            }
            return;
        }

        double activeStart = fx.aux.y;
        double activeEnd = activeStart + (fx.duration - activeStart) * 0.8;
        double activeAge = age - activeStart;
        double activeProgress = clamp01(activeAge / Math.max(1, activeEnd - activeStart));

        // Photon ring
        Vec3 toCam = cam.subtract(c).normalize();
        g.color(1.0f, 0.4f, 0.8f, (float) (0.8 * (1.0 - activeProgress * 0.2))).ring(c, toCam, r * 1.01, r * 0.025, seg, 0, false);
        g.color(1.0f, 0.8f, 0.3f, (float) (0.4 * (1.0 - activeProgress * 0.2))).ring(c, toCam, r * 1.3, r * 0.12, seg, 0, false);

        // Quasar beams (visual only - actual beam in drawBeam)
        // Rotating energy rings
        for (int i = 0; i < 5; i++) {
            double phase = activeAge * (0.012 + i * 0.004) + i * 1.2;
            Vec3 normal = new Vec3(Math.sin(0.3 + i * 0.15) * Math.cos(i * 1.3), Math.cos(0.3 + i * 0.15), Math.sin(0.3 + i * 0.15) * Math.sin(i * 1.3)).normalize();
            g.color(1.0f, (float) lerp(0.4, 0.8, i * 0.2), (float) lerp(0.7, 0.2, i * 0.2), (float) (0.35 * (1.0 - i * 0.1) * (1.0 - activeProgress * 0.15)))
                    .ring(c, normal, r * (1.02 + i * 0.06), r * (0.012 + i * 0.008), seg, phase, true);
        }

        // Collapse
        if (age >= activeEnd) {
            double cp = clamp01((age - activeEnd) / Math.max(1, fx.duration - activeEnd));
            double implR = r * (1.0 - cp * 0.9);
            float fa = (float) (1.0 - cp);
            if (implR > 0.02) {
                g.color(1.0f, 0.9f, 0.5f, fa).sphere(c, implR * 1.8, 8, 16);
                g.color(1.0f, 0.3f, 0.6f, fa * 0.7).sphere(c, implR, 12, seg);
                for (int i = 0; i < 4; i++) {
                    double ringR = implR + cp * r * (0.4 + i * 0.25);
                    g.color(1.0f, 0.4f, 0.8f, (float) ((1.0 - cp) * (0.5 - i * 0.1))).ring(c, new Vec3(0, 1, 0), ringR, r * 0.06, seg, 0, false);
                }
            }
        }
    }

    private static void voidBlackHoleGlow(Effect fx, double age, Geo g, Vec3 cam, int seg) {
        Vec3 c = fx.pos;
        double r = currentRadius(fx, age);
        double form = clamp01(age / Math.max(1.0, fx.aux.y));
        double ease = smoothstep(form);

        if (age < fx.aux.y) {
            double coreR = 0.6 * ease;
            Vec3 corePos = c.add(0, 1.5, 0);
            g.color(0.3f, 0.0f, 0.5f, (float) (0.95 * ease)).sphere(corePos, coreR, 10, seg / 2);
            // Void particles - swirling darkness
            for (int i = 0; i < 5; i++) {
                double ringPhase = age * 0.02 + i * 1.25;
                double tilt = Math.sin(ringPhase * 0.3) * 0.5;
                Vec3 normal = new Vec3(Math.sin(tilt) * Math.cos(i * 1.25), Math.cos(tilt), Math.sin(tilt) * Math.sin(i * 1.25)).normalize();
                g.color(0.2f, 0.0f, 0.4f, (float) (0.5 * ease * (1.0 - i * 0.15))).ring(corePos, normal, coreR * (1.15 + i * 0.35), coreR * 0.07, seg, ringPhase, true);
            }
            return;
        }

        double activeStart = fx.aux.y;
        double activeEnd = activeStart + (fx.duration - activeStart) * 0.8;
        double activeAge = age - activeStart;
        double activeProgress = clamp01(activeAge / Math.max(1, activeEnd - activeStart));

        // Void lensing - darker, more intense
        Vec3 toCam = cam.subtract(c).normalize();
        g.color(0.15f, 0.0f, 0.3f, (float) (0.9 * (1.0 - activeProgress * 0.1))).ring(c, toCam, r * 1.01, r * 0.03, seg, 0, false);
        g.color(0.4f, 0.1f, 0.6f, (float) (0.5 * (1.0 - activeProgress * 0.1))).ring(c, toCam, r * 1.15, r * 0.08, seg, 0, false);
        g.color(0.6f, 0.0f, 0.8f, (float) (0.3 * (1.0 - activeProgress * 0.1))).ring(c, toCam, r * 1.4, r * 0.04, seg, 0, false);

        // Void accretion - dark matter spirals
        Vec3 diskNormal = new Vec3(Math.sin(0.5) * Math.cos(0.8), Math.cos(0.5), Math.sin(0.5) * Math.sin(0.8)).normalize();
        for (int k = 0; k < 16; k++) {
            double u = k / 15.0;
            double radius = r * (1.3 + 4.0 * u);
            double omega = 0.25 / Math.pow(1.0 + 2.0 * u, 1.5);
            g.color((float) lerp(0.3, 0.6, u), (float) lerp(0.0, 0.2, u), (float) lerp(0.5, 0.8, u), (float) ((0.85 - 0.55 * u) * (1.0 - activeProgress * 0.1)))
                    .ring(c, diskNormal, radius, r * (0.05 + 0.08 * u), seg, activeAge * omega + k * 0.5, true);
        }

        // Void tendrils
        for (int arm = 0; arm < 8; arm++) {
            double base = arm * Math.PI * 2.0 / 8;
            Vec3 prev = null;
            for (int j = 0; j <= 20; j++) {
                double s = j / 20.0;
                double rad = r * 1.1 + (fx.aux.x * 0.7 - r * 1.1) * Math.pow(1.0 - s, 1.8);
                double ang = base + age * 0.05 + s * 5.5;
                double h = Math.sin(base * 2.5 + s * 4.0) * (1.0 - s) * fx.aux.x * 0.12;
                Vec3 pt = c.add(Math.cos(ang) * rad, h, Math.sin(ang) * rad);
                if (prev != null) {
                    g.color((float) lerp(0.4, 0.8, s), (float) lerp(0.0, 0.3, s), (float) lerp(0.6, 1.0, s), (float) (0.5 * (1.0 - activeProgress * 0.1) * (0.3 + 0.7 * s)));
                    g.ribbon(prev, pt, 0.03 + 0.05 * s);
                }
                prev = pt;
            }
        }

        // Collapse - void implosion
        if (age >= activeEnd) {
            double cp = clamp01((age - activeEnd) / Math.max(1, fx.duration - activeEnd));
            double implR = r * (1.0 - cp * 0.95);
            float fa = (float) (1.0 - cp);
            if (implR > 0.02) {
                g.color(0.2f, 0.0f, 0.4f, fa * 0.8).sphere(c, implR * 2.0, 8, 16);
                g.color(0.1f, 0.0f, 0.2f, fa * 0.9).sphere(c, implR, 12, seg);
                for (int i = 0; i < 5; i++) {
                    double ringR = implR + cp * r * (0.6 + i * 0.2);
                    g.color(0.3f, 0.0f, 0.5f, (float) ((1.0 - cp) * (0.4 - i * 0.07))).ring(c, new Vec3(0, 1, 0), ringR, r * 0.07, seg, 0, false);
                }
            }
        }
    }

    private static void eventHorizonGlow(Effect fx, double age, Geo g, Vec3 cam, int seg) {
        Vec3 c = fx.pos;
        double r = currentRadius(fx, age);
        double form = clamp01(age / Math.max(1.0, fx.aux.y));
        double ease = smoothstep(form);

        // Formation: expanding shield
        if (age < fx.aux.y) {
            double shieldR = r;
            g.color(0.2f, 0.5f, 1.0f, (float) (0.3 * ease)).sphere(c, shieldR * 1.02, 12, seg);
            g.color(0.1f, 0.4f, 0.9f, (float) (0.5 * ease)).sphere(c, shieldR, 16, seg);
            for (int i = 0; i < 3; i++) {
                double ringPhase = age * 0.03 + i * 2.0;
                g.color(0.2f, 0.6f, 1.0f, (float) (0.4 * ease * (1.0 - i * 0.2))).ring(c, new Vec3(0, 1, 0), shieldR * (1.0 + i * 0.05), shieldR * 0.02, seg, ringPhase, true);
            }
            return;
        }

        double activeStart = fx.aux.y;
        double activeEnd = activeStart + (fx.duration - activeStart) * 0.8;
        double activeAge = age - activeStart;
        double activeProgress = clamp01(activeAge / Math.max(1, activeEnd - activeStart));

        // Protective shimmer
        Vec3 toCam = cam.subtract(c).normalize();
        g.color(0.2f, 0.7f, 1.0f, (float) (0.4 * (1.0 - activeProgress * 0.2))).ring(c, toCam, r * 1.01, r * 0.015, seg, 0, false);

        // Hexagonal shield pattern
        for (int i = 0; i < 6; i++) {
            double tilt = 0.3 + i * 0.1;
            Vec3 normal = new Vec3(Math.sin(tilt) * Math.cos(i * 1.047), Math.cos(tilt), Math.sin(tilt) * Math.sin(i * 1.047)).normalize();
            double phase = activeAge * 0.005 + i * 1.047;
            g.color(0.2f, 0.6f, 1.0f, (float) (0.25 * (1.0 - activeProgress * 0.15))).ring(c, normal, r * (1.01 + i * 0.02), r * 0.01, seg, phase, true);
        }

        // Collapse - gentle fade
        if (age >= activeEnd) {
            double cp = clamp01((age - activeEnd) / Math.max(1, fx.duration - activeEnd));
            double implR = r * (1.0 - cp);
            float fa = (float) (1.0 - cp);
            if (implR > 0.02) {
                g.color(0.2f, 0.7f, 1.0f, fa * 0.6).sphere(c, implR, 12, seg);
                for (int i = 0; i < 2; i++) {
                    g.color(0.2f, 0.6f, 1.0f, fa * (0.3 - i * 0.1)).ring(c, new Vec3(0, 1, 0), implR * (1.0 + i * 0.1), implR * 0.02, seg, 0, false);
                }
            }
        }
    }

    private static void gravitationalWaveGlow(Effect fx, double age, Geo g, Vec3 cam, int seg) {
        // Waves don't have persistent glow - handled in drawWave
        double total = fx.duration;
        double activeStart = fx.aux.y;
        if (age < activeStart) {
            // Formation: small pulse
            double coreR = 0.3 * clamp01(age / Math.max(1.0, activeStart));
            g.color(0.7f, 0.8f, 1.0f, (float) (0.5 * coreR)).sphere(fx.pos, coreR, 8, seg / 2);
        }
    }

    // ===== Beam rendering (Quasar) =====
    private static void drawBeam(Effect fx, double age, Geo g, Vec3 cam) {
        if (fx.type != FxType.QUASAR_BLACK_HOLE && fx.type != FxType.QUASAR_BLACK_HOLE_COLLAPSE) return;

        double form = clamp01(age / Math.max(1.0, fx.aux.y));
        if (form < 1.0) return;

        double activeStart = fx.aux.y;
        double activeEnd = activeStart + (fx.duration - activeStart) * 0.8;
        if (age < activeStart || age > activeEnd) return;

        double activeAge = age - activeStart;
        Vec3 c = fx.pos;
        double beamRange = 30.0;

        // Beam direction (straight down from centre)
        Vec3 beamDir = new Vec3(0, -1, 0);

        // Pulsing beam
        double pulse = 0.7 + 0.3 * Math.sin(activeAge * 0.3);
        int segs = 8;
        g.color(1.0f, 0.4f, 0.8f, (float) (0.6 * pulse)).beam(c, beamDir, beamRange * pulse, 0.8, segs);
        g.color(1.0f, 0.8f, 0.3f, (float) (0.4 * pulse)).beam(c, beamDir, beamRange * pulse * 0.7, 0.4, segs);
        g.color(1.0f, 0.2f, 0.6f, (float) (0.8 * pulse)).beam(c, beamDir, beamRange * pulse * 1.2, 1.2, segs);
    }

    // ===== Wave ripple rendering (Gravitational Wave) =====
    private static void drawWave(Effect fx, double age, Geo g, Vec3 cam) {
        if (fx.type != FxType.GRAVITATIONAL_WAVE && fx.type != FxType.GRAVITATIONAL_WAVE_COLLAPSE &&
            fx.type != FxType.GRAVITATIONAL_WAVE_RIPPLE) return;

        Vec3 c = fx.pos;
        int seg = segments(fx, cam);

        if (fx.type == FxType.GRAVITATIONAL_WAVE_RIPPLE) {
            // Individual ripple from aux data
            double rippleRadius = fx.aux.x;
            int waveIndex = (int) fx.aux.z;
            double waveAge = age;
            double maxRadius = 24.0;
            double progress = rippleRadius / maxRadius;
            if (progress > 1.0) return;

            float alpha = (float) ((1.0 - progress) * 0.4);
            double thickness = 0.1 + 0.3 * progress;
            g.color(0.7f, 0.8f, 1.0f, alpha).ring(c, new Vec3(0, 1, 0), rippleRadius, thickness, seg, 0, false);
            g.color(0.5f, 0.7f, 1.0f, alpha * 0.7).ring(c, new Vec3(0, 1, 0), rippleRadius * 1.1, thickness * 0.7, seg, 0, false);
            g.color(0.5f, 0.7f, 1.0f, alpha * 0.7).ring(c, new Vec3(1, 0, 0), rippleRadius * 1.1, thickness * 0.7, seg, 0, false);
            return;
        }

        if (fx.type == FxType.GRAVITATIONAL_WAVE_COLLAPSE) {
            // Final massive wave
            double t = clamp01(age / Math.max(1, fx.duration));
            double shellR = 24.0 * t * 1.5;
            float fade = (float) (1.0 - t);
            g.color(0.7f, 0.8f, 1.0f, 0.15f * fade).sphere(c, shellR, 12, seg / 2);
            double band = 0.15 + 0.5 * fade;
            g.color(0.6f, 0.8f, 1.0f, 0.8f * fade).ring(c, new Vec3(0, 1, 0), shellR, band, seg, 0, false);
            g.color(0.5f, 0.7f, 1.0f, 0.6f * fade).ring(c, new Vec3(1, 0, 0), shellR, band * 0.7, seg, 0, false);
            return;
        }
    }

    // ===== Generic collapse glow =====
    private static void collapseGlow(Effect fx, double age, Geo g, Vec3 cam, int seg, ColorScheme cs) {
        double t = clamp01(age / Math.max(1, fx.duration));
        Vec3 c = fx.pos;
        double maxR = fx.aux.x;

        if (t < 0.3) {
            double flashR = maxR * 0.4 * (1.0 - t / 0.3);
            g.color(1.0f, 0.95f, 0.8f, (float) ((1.0 - t / 0.3) * 0.9)).sphere(c, flashR, 8, 16);
        }

        double shellR = maxR * t * 1.2;
        float fade = (float) (1.0 - t);
        g.color(cs.coreR, cs.coreG, cs.coreB, 0.15f * fade).sphere(c, shellR, 12, seg / 2);

        double band = 0.1 + 0.4 * fade;
        g.color(cs.coreR, cs.coreG, cs.coreB, 0.7f * fade).ring(c, new Vec3(0, 1, 0), shellR, band, seg, 0, false);
        g.color(lerp(cs.coreR, 0.7f, 0.5), lerp(cs.coreG, 0.8f, 0.5), lerp(cs.coreB, 1.0f, 0.5), 0.5f * fade).ring(c, new Vec3(1, 0, 0), shellR, band * 0.7, seg, 0, false);
        g.color(lerp(cs.coreR, 0.7f, 0.5), lerp(cs.coreG, 0.8f, 0.5), lerp(cs.coreB, 1.0f, 0.5), 0.5f * fade).ring(c, new Vec3(0, 0, 1), shellR, band * 0.7, seg, 0, false);
        g.color(1.0f, 0.95f, 0.8f, 0.7f * fade).ring(c, cam.subtract(c).normalize(), shellR * 0.8, band * 0.5, seg, 0, false);
    }
}