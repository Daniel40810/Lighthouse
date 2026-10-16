package com.dan.lighthouse.wiring;

/** Ein Ereignis für den Klang: Brecher, Nebelhorn, Möwe, Donner, Typhon eines Schiffs (Phase 13). Gibt: spray, fog, flock, lightning. Nimmt: {@code soundscape} (Phase 14). */
@FunctionalInterface
public interface SoundEvent {
    enum Kind { BRECHER, NEBELHORN, MOEWE, DONNER, REGEN_GLAS, TYFON }

    void sound(Kind kind, double x, double y, double z, double strength);
}
