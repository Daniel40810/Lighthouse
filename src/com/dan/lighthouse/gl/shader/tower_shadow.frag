// tower_shadow.frag — nur Tiefe; Glas lässt das Licht durch (Laternenraum, Phase 8).
flat in int vKind;
void main() { if (vKind == 2) discard; }
