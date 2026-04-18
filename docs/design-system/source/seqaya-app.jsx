// Main composition: canvas + design-system preamble + all 22 screens arranged.

const { useState } = React;

const DesignSystem = () => (
  <div className="band">
    <div className="band-head">
      <div className="tag">§ 01 — Canon</div>
      <div>
        <h2>The system</h2>
        <p>A small set of tokens and parts, used with restraint. Cream does the work. Terracotta and sage only speak when they have something to say.</p>
      </div>
    </div>

    <div className="ds-section">
      <div className="ds-title">Palette — light</div>
      <h3 className="ds-h">Cream, dark ink, two accents.</h3>
      <div className="swatches">
        {[
          ['Cream', '#faf9f5', 'bg.cream', true],
          ['Cream light', '#e8e6dc', 'bg.cream.light'],
          ['Ink', '#141413', 'text.primary', false, '#faf9f5'],
          ['Muted', '#75736b', 'text.secondary', false, '#faf9f5'],
          ['Mute deep', '#b0aea5', 'text.tertiary'],
          ['Sage', '#788c5d', 'accent.green', false, '#faf9f5'],
          ['Terracotta', '#d97757', 'accent.brown', false, '#faf9f5'],
          ['Fog blue', '#6a9bcc', 'accent.blue', false, '#faf9f5'],
        ].map(([n, h, t]) => (
          <div className="sw" key={n}>
            <div className="sw-chip" style={{ background: h }}/>
            <div className="sw-name">{n}</div>
            <div className="sw-hex">{h}</div>
            <div className="sw-hex" style={{ color: 'var(--text-tertiary)' }}>{t}</div>
          </div>
        ))}
      </div>
    </div>

    <div className="ds-section">
      <div className="ds-title">Typography</div>
      <h3 className="ds-h">Fraunces for feeling, Inter for work.</h3>
      <div className="kv"><div className="k">Display XL · 72pt</div><div className="v" style={{ fontFamily: 'var(--serif)', fontSize: 52, lineHeight: 1, fontWeight: 500, letterSpacing: '-0.025em', fontVariationSettings: '"opsz" 144' }}>62<span style={{ fontSize: 26, color: 'var(--text-tertiary)' }}>%</span></div><div className="sample">Fraunces · opsz 144</div></div>
      <div className="kv"><div className="k">Display L · 32pt</div><div className="v" style={{ fontFamily: 'var(--serif)', fontSize: 30, fontWeight: 500, letterSpacing: '-0.015em', fontVariationSettings: '"opsz" 72' }}>Care, quietly.</div><div className="sample">Fraunces · opsz 72</div></div>
      <div className="kv"><div className="k">Heading · 20pt</div><div className="v" style={{ fontFamily: 'var(--serif)', fontSize: 20, fontWeight: 500, fontVariationSettings: '"opsz" 24' }}>Care for your fig</div><div className="sample">Fraunces · opsz 24</div></div>
      <div className="kv"><div className="k">Body · 16pt</div><div className="v" style={{ fontSize: 15, lineHeight: 1.55 }}>The top inch of soil should feel dry before you water.</div><div className="sample">Inter · 400 · tracking 0</div></div>
      <div className="kv"><div className="k">Caption · 13pt</div><div className="v" style={{ fontSize: 13, color: 'var(--text-secondary)' }}>Last watered 3h ago · Thriving</div><div className="sample">Inter · 500</div></div>
      <div className="kv"><div className="k">Label caps · 10pt</div><div className="v" style={{ fontFamily: 'var(--mono)', fontSize: 10, letterSpacing: '0.16em', textTransform: 'uppercase', color: 'var(--text-secondary)' }}>Step 2 of 3</div><div className="sample">JetBrains Mono</div></div>
    </div>
  </div>
);

const Canvas = () => (
  <div className="canvas">
    <header className="canvas-header">
      <div>
        <div className="eyebrow">Seqaya · v2 · Design study, April 2026</div>
        <h1>A quieter<br/>house of plants.</h1>
        <p className="lede">
          Twenty-two screens for an Android app that waters your plants while you live your life. Editorial warmth, hairlines over shadows, serif where it matters, terracotta and sage used only when they carry meaning.
        </p>
      </div>
      <div className="meta">
        <div>Density · <b>3-up canvas</b></div>
        <div>Fonts · <b>Fraunces · Inter</b></div>
        <div>Surface · <b>#faf9f5 cream</b></div>
        <div>Accents · <b>#d97757 · #788c5d</b></div>
        <div>Frame · <b>Android · 360×760</b></div>
        <div>Screens · <b>22 + 5 shared</b></div>
      </div>
    </header>

    <DesignSystem/>

    <div className="band">
      <div className="band-head">
        <div className="tag">§ 02 — Auth</div>
        <div>
          <h2>Arriving.</h2>
          <p>A wordmark, a breathing leaf, one door. The first two screens say almost nothing, on purpose.</p>
        </div>
      </div>
      <div className="screens">
        <A1_Splash/>
        <A2_SignIn/>
        <div/>
      </div>
    </div>

    <div className="band">
      <div className="band-head">
        <div className="tag">§ 03 — Home</div>
        <div>
          <h2>Checking in.</h2>
          <p>A short list of plants, with their moisture lately. If one is unhappy, a terracotta line appears above the rest. Adding a device is three small steps — plant, Wi-Fi, tap.</p>
        </div>
      </div>
      <div className="screens">
        <B1_HomeEmpty/>
        <B2_HomePop/>
        <B3_DeviceDetail/>
        <B4_PickPlant/>
        <B5_WiFi/>
        <B6_TapPhone/>
      </div>
    </div>

    <div className="band">
      <div className="band-head">
        <div className="tag">§ 04 — Scan</div>
        <div>
          <h2>Looking closely.</h2>
          <p>Point the camera at a leaf. Three orbiting leaves mark the wait. A confident match, or a candid "not sure."</p>
        </div>
      </div>
      <div className="screens">
        <C1_Camera/>
        <C2_Analyzing/>
        <C3_Result/>
        <C4_NoMatch/>
        <div/>
        <div/>
      </div>
    </div>

    <div className="band">
      <div className="band-head">
        <div className="tag">§ 05 — Library</div>
        <div>
          <h2>A small field guide.</h2>
          <p>Categories as type, not pills-in-a-pill-rack. Each plant has a hand-drawn mark, its Latin name in italic, and notes about its troubles.</p>
        </div>
      </div>
      <div className="screens">
        <D1_Browse/>
        <D2_PlantDetail/>
        <D3_Search/>
      </div>
    </div>

    <div className="band">
      <div className="band-head">
        <div className="tag">§ 06 — Settings & profile</div>
        <div>
          <h2>The quiet pages.</h2>
          <p>Grouped rows, one column, no tabs-within-tabs. The danger zone is a single line in terracotta.</p>
        </div>
      </div>
      <div className="screens">
        <E1_Settings/>
        <E2_Profile/>
        <E3_DeleteAccount/>
      </div>
    </div>

    <div className="band">
      <div className="band-head">
        <div className="tag">§ 07 — Shared parts</div>
        <div>
          <h2>Used more than once.</h2>
          <p>Offline banner, toast, bottom sheets, empty states. Designed once so the rest of the app can stay short.</p>
        </div>
      </div>
      <div className="screens">
        <F1_Offline/>
        <F2_Toast/>
        <F3_DeviceSheet/>
        <F4_PlantSheet/>
        <F5_Empty/>
        <div/>
      </div>
    </div>

    <div className="band">
      <div className="band-head">
        <div className="tag">§ 08 — After dark</div>
        <div>
          <h2>The cream inverts.</h2>
          <p>Ink becomes cream, cream becomes near-black, sage and terracotta lighten a touch to keep their contrast. Five screens to prove the system holds.</p>
        </div>
      </div>
      <div className="screens">
        <B2_HomePop dark/>
        <B3_DeviceDetail dark/>
        <D1_Browse dark/>
        <E1_Settings dark/>
        <div/>
        <div/>
      </div>
    </div>

    <div className="band">
      <div className="band-head">
        <div className="tag">§ 09 — Contextual sheets</div>
        <div>
          <h2>The moment of recognition.</h2>
          <p>The user is holding a device. They don't need to find it — they need to know which of their plants it is. One bottom sheet, not a modal stack. The serial answers <em>which</em>; the identity card answers <em>recognition</em>; the button answers <em>what next</em>.</p>
        </div>
      </div>
      <div className="screens">
        <G1_Locate/>
        <G2_Hold/>
        <G3_HoldConfirm/>
        <G4_Resume/>
        <G5_ResumeConfirm/>
        <G_Illustrations/>
        <G_StatesDiagram/>
      </div>
    </div>

    <footer style={{ marginTop: 120, paddingTop: 40, borderTop: '1px solid var(--border-strong)', display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', fontFamily: 'var(--mono)', fontSize: 11, letterSpacing: '0.12em', textTransform: 'uppercase', color: 'var(--text-secondary)' }}>
      <span>Seqaya · design study</span>
      <span style={{ fontFamily: 'var(--serif)', fontStyle: 'italic', fontSize: 16, textTransform: 'none', letterSpacing: 0 }}>Made for the windowsill, not the warehouse.</span>
      <span>April 2026</span>
    </footer>
  </div>
);

ReactDOM.createRoot(document.getElementById('root')).render(<Canvas/>);
