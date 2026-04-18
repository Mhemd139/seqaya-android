// G. Contextual NFC sheets — Locate, Hold, Hold-confirmation, Resume, Resume-confirmation

// Shared leaf illustrations — closed & opening
const ClosedBud = ({ size = 120 }) => (
  <svg width={size} height={size} viewBox="0 0 120 120" fill="none" stroke="#3e3d38" strokeWidth="1.3" strokeLinecap="round" strokeLinejoin="round" role="img" aria-label="Closed leaf illustration representing paused care">
    <path d="M60 98V60" />
    <path d="M60 60c-8-2-14-10-14-22 0-10 5-18 14-22 9 4 14 12 14 22 0 12-6 20-14 22z" fill="#788c5d" fillOpacity="0.2"/>
    <path d="M60 16v44M54 28c2 4 4 8 6 14M66 28c-2 4-4 8-6 14"/>
    <path d="M50 98h20l-2 10H52z" fill="#d97757" fillOpacity="0.1"/>
    <path d="M50 98h20l-2 10H52z"/>
  </svg>
);

const OpeningLeaf = ({ size = 120 }) => (
  <svg width={size} height={size} viewBox="0 0 120 120" fill="none" stroke="#3e3d38" strokeWidth="1.3" strokeLinecap="round" strokeLinejoin="round" role="img" aria-label="Opening leaf illustration representing resumed care">
    <path d="M60 98V58" />
    <path d="M60 58c-14 0-24-10-26-24 12-2 24 6 26 18z" fill="#788c5d" fillOpacity="0.22"/>
    <path d="M60 58c14 0 24-10 26-24-12-2-24 6-26 18z" fill="#788c5d" fillOpacity="0.22"/>
    <path d="M60 58c-2-8 2-18 8-24M60 58c2-8-2-18-8-24"/>
    <path d="M42 44c4 2 10 6 18 14M78 44c-4 2-10 6-18 14"/>
    <path d="M50 98h20l-2 10H52z"/>
  </svg>
);

const WateringCan = ({ size = 120 }) => (
  <svg width={size} height={size} viewBox="0 0 120 120" fill="none" stroke="#3e3d38" strokeWidth="1.3" strokeLinecap="round" strokeLinejoin="round" role="img" aria-label="Watering can tilted away">
    <g transform="rotate(-18 60 66)">
      <path d="M32 52h44v32a4 4 0 01-4 4H36a4 4 0 01-4-4V52z" fill="#d97757" fillOpacity="0.12"/>
      <path d="M32 52h44v32a4 4 0 01-4 4H36a4 4 0 01-4-4V52z"/>
      <path d="M32 52l-14-8v-4h20"/>
      <path d="M76 60c6 0 12 2 14 6"/>
      <path d="M46 44v-8h16v8"/>
    </g>
  </svg>
);

// Reusable identity card used by Locate/Hold/Resume
const IdentityCard = ({ name, species, location, added, moisture, lastWater, state, stateColor, history, paused = false }) => (
  <>
    <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'space-between', gap: 12 }}>
      <div style={{ fontFamily: 'var(--serif)', fontSize: 38, fontWeight: 500, letterSpacing: '-0.02em', lineHeight: 1.05, fontVariationSettings: '"opsz" 96' }}>{name}</div>
    </div>
    <div className="muted" style={{ fontSize: 12.5, marginTop: 6, fontFamily: 'var(--sans)' }}>
      {species}{location ? ' · ' + location : ''}{added ? ' · ' + added : ''}
    </div>
    {paused && (
      <div style={{ display: 'inline-block', marginTop: 10, padding: '4px 10px', borderRadius: 100, background: 'var(--bg-cream-light)', border: '1px solid var(--border-strong)', fontFamily: 'var(--mono)', fontSize: 9.5, letterSpacing: '0.18em', textTransform: 'uppercase', color: 'var(--text-secondary)' }}>
        Currently paused
      </div>
    )}
    {/* Photo / sketch */}
    <div style={{ marginTop: 22, width: '100%', aspectRatio: '4 / 3', borderRadius: 18, border: '1px solid var(--border)', background: 'linear-gradient(135deg, #e8e6dc 0%, #d8d6cc 100%)', overflow: 'hidden', position: 'relative', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
      <PlantSketch kind="pothos" size={140}/>
      <div style={{ position: 'absolute', bottom: 10, right: 12, fontFamily: 'var(--mono)', fontSize: 9, letterSpacing: '0.14em', textTransform: 'uppercase', color: 'var(--text-tertiary)', background: 'rgba(250,249,245,0.82)', padding: '3px 7px', borderRadius: 100 }}>setup photo</div>
    </div>
    {/* State ribbon */}
    <div style={{ marginTop: 22, display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 10 }}>
      {[
        ['Moisture', moisture, 'var(--text-primary)'],
        ['Last watered', lastWater, 'var(--text-primary)'],
        ['State', state, stateColor],
      ].map(([l, v, c], i) => (
        <div key={i}>
          <div className="caps" style={{ fontSize: 9 }}>{l}</div>
          <div style={{ fontFamily: 'var(--serif)', fontSize: 21, fontWeight: 500, letterSpacing: '-0.01em', marginTop: 4, color: c, fontVariationSettings: '"opsz" 36' }}>{v}</div>
        </div>
      ))}
    </div>
    {/* History line */}
    <div style={{ marginTop: 18, paddingTop: 16, borderTop: '1px solid var(--border)', fontFamily: 'var(--serif)', fontStyle: 'italic', fontSize: 15, lineHeight: 1.5, color: 'var(--text-secondary)' }}>
      {history}
    </div>
  </>
);

// Sheet wrapper — uses phone frame but rendered full-sheet (90% height) over a faded backdrop
const SheetPhone = ({ children, backdrop }) => (
  <div className="phone grain">
    <div className="phone-inner">
      <StatusBar/>
      <div className="content" style={{ position: 'relative' }}>
        {/* faded backdrop hint */}
        <div style={{ position: 'absolute', inset: 0, opacity: 0.28, pointerEvents: 'none' }}>
          {backdrop}
        </div>
        {/* sheet */}
        <div style={{ position: 'absolute', left: 0, right: 0, bottom: 0, top: 48, background: 'var(--bg-cream)', borderTopLeftRadius: 24, borderTopRightRadius: 24, borderTop: '1px solid var(--border-strong)', display: 'flex', flexDirection: 'column', zIndex: 2 }}>
          <div style={{ width: 40, height: 4, borderRadius: 2, background: 'var(--text-tertiary)', margin: '10px auto 2px' }}/>
          {children}
        </div>
      </div>
    </div>
    <div className="hp"/>
  </div>
);

const BackdropHome = () => (
  <div>
    <div className="topbar"><span className="wordmark">Seqaya</span><div className="avatar">M</div></div>
    <div style={{ padding: '0 20px' }}>
      <div className="card" style={{ marginBottom: 12 }}>
        <div className="h-m">Lucy</div>
        <div className="italic muted" style={{ fontSize: 12 }}>Epipremnum aureum</div>
      </div>
    </div>
  </div>
);

const G1_Locate = () => (
  <Screen id="G · 01" name="Locate — which plant?">
    <SheetPhone backdrop={<BackdropHome/>}>
      <div style={{ padding: '6px 18px 0' }}>
        <div style={{ display: 'flex', justifyContent: 'flex-start' }}>
          <div className="icon-btn"><I.close size={20}/></div>
        </div>
      </div>
      <div style={{ flex: 1, overflowY: 'auto', padding: '4px 22px 0' }}>
        <IdentityCard
          name="Lucy"
          species="Pothos"
          location="south window"
          added="added 3 months ago"
          moisture="62%"
          lastWater="3h ago"
          state="Thriving"
          stateColor="var(--accent-green)"
          history="You and Lucy have been at this for 94 days."
        />
      </div>
      <div style={{ padding: '16px 22px 20px', display: 'flex', flexDirection: 'column', gap: 8 }}>
        <div className="btn btn-primary">Yes, this is Lucy</div>
        <div style={{ textAlign: 'center', padding: '10px 0 2px' }}>
          <span className="btn-text" style={{ fontSize: 13 }}>Something's wrong</span>
        </div>
      </div>
    </SheetPhone>
  </Screen>
);

const G2_Hold = () => (
  <Screen id="G · 02" name="Hold — pause Lucy">
    <SheetPhone backdrop={<BackdropHome/>}>
      <div style={{ padding: '6px 18px 0' }}>
        <div style={{ display: 'flex', justifyContent: 'flex-start' }}>
          <div className="icon-btn"><I.close size={20}/></div>
        </div>
      </div>
      <div style={{ flex: 1, overflowY: 'auto', padding: '4px 22px 0' }}>
        <IdentityCard
          name="Lucy"
          species="Pothos"
          location="south window"
          added="added 3 months ago"
          moisture="62%"
          lastWater="3h ago"
          state="Thriving"
          stateColor="var(--accent-green)"
          history="The pot is still heavy from this morning — is that why you're here?"
        />
      </div>
      <div style={{ padding: '16px 22px 20px', display: 'flex', flexDirection: 'column', gap: 8 }}>
        <div className="btn btn-primary">Pause watering</div>
        <div style={{ textAlign: 'center', padding: '10px 0 2px' }}>
          <span className="btn-text" style={{ fontSize: 13 }}>Not this one — locate a different plant</span>
        </div>
      </div>
    </SheetPhone>
  </Screen>
);

const G3_HoldConfirm = () => (
  <Screen id="G · 03" name="Hold — paused">
    <SheetPhone backdrop={<BackdropHome/>}>
      <div style={{ padding: '6px 18px 0' }}>
        <div style={{ display: 'flex', justifyContent: 'flex-start' }}>
          <div className="icon-btn"><I.close size={20}/></div>
        </div>
      </div>
      <div style={{ flex: 1, overflowY: 'auto', padding: '8px 28px 0', display: 'flex', flexDirection: 'column', alignItems: 'center', textAlign: 'center', gap: 18 }}>
        <div style={{ fontFamily: 'var(--serif)', fontSize: 32, fontWeight: 500, letterSpacing: '-0.02em', marginTop: 8 }}>Paused.</div>
        <ClosedBud size={140}/>
        <div className="body-t" style={{ color: 'var(--text-secondary)', maxWidth: '28ch' }}>
          Lucy is on her own until you resume. We'll still track her moisture so you can see what happens.
        </div>
      </div>
      <div style={{ padding: '18px 22px 20px', display: 'flex', flexDirection: 'column', gap: 8 }}>
        <div className="btn btn-primary">Done</div>
        <div style={{ textAlign: 'center', padding: '10px 0 2px' }}>
          <span className="btn-text" style={{ fontSize: 13 }}>Resume watering now</span>
        </div>
      </div>
    </SheetPhone>
  </Screen>
);

const G4_Resume = () => (
  <Screen id="G · 04" name="Resume — wake Lucy">
    <SheetPhone backdrop={<BackdropHome/>}>
      <div style={{ padding: '6px 18px 0' }}>
        <div style={{ display: 'flex', justifyContent: 'flex-start' }}>
          <div className="icon-btn"><I.close size={20}/></div>
        </div>
      </div>
      <div style={{ flex: 1, overflowY: 'auto', padding: '4px 22px 0' }}>
        <IdentityCard
          name="Lucy"
          species="Pothos"
          location="south window"
          added="added 3 months ago"
          moisture="41%"
          lastWater="6d ago"
          state="Paused"
          stateColor="var(--text-secondary)"
          paused
          history={<>Paused since Tuesday. Lucy's moisture has drifted from 58% to 41%.</>}
        />
      </div>
      <div style={{ padding: '16px 22px 20px', display: 'flex', flexDirection: 'column', gap: 8 }}>
        <div className="btn" style={{ background: 'var(--accent-green)', color: 'var(--bg-cream)' }}>Resume watering</div>
        <div style={{ textAlign: 'center', padding: '10px 0 2px' }}>
          <span className="btn-text" style={{ fontSize: 13 }}>Keep paused</span>
        </div>
      </div>
    </SheetPhone>
  </Screen>
);

const G5_ResumeConfirm = () => (
  <Screen id="G · 05" name="Resume — welcome back">
    <SheetPhone backdrop={<BackdropHome/>}>
      <div style={{ padding: '6px 18px 0' }}>
        <div style={{ display: 'flex', justifyContent: 'flex-start' }}>
          <div className="icon-btn"><I.close size={20}/></div>
        </div>
      </div>
      <div style={{ flex: 1, overflowY: 'auto', padding: '8px 28px 0', display: 'flex', flexDirection: 'column', alignItems: 'center', textAlign: 'center', gap: 18 }}>
        <div style={{ fontFamily: 'var(--serif)', fontSize: 32, fontWeight: 500, letterSpacing: '-0.02em', marginTop: 8 }}>Welcome back.</div>
        <OpeningLeaf size={140}/>
        <div className="body-t" style={{ color: 'var(--text-secondary)', maxWidth: '28ch' }}>
          Lucy is on her own schedule again. We'll check back on her in 5 hours.
        </div>
      </div>
      <div style={{ padding: '18px 22px 20px' }}>
        <div className="btn btn-primary">Done</div>
      </div>
    </SheetPhone>
  </Screen>
);

// A small states diagram frame
const G_StatesDiagram = () => (
  <div className="screen" style={{ gridColumn: 'span 2' }}>
    <div style={{ background: 'var(--bg-cream)', border: '1px solid var(--border)', borderRadius: 24, padding: 36, minHeight: 760 }}>
      <div className="caps" style={{ color: 'var(--accent-brown)', marginBottom: 6 }}>State machine</div>
      <div className="h-l" style={{ fontSize: 28, marginBottom: 6 }}>After a tap, which sheet?</div>
      <div className="italic muted" style={{ fontSize: 14, marginBottom: 28 }}>One bottom sheet, not a modal stack.</div>

      {[
        ['01', 'Unregistered serial', 'Device is new to this account', 'Add Device wizard (B4 → B6)', 'var(--accent-brown)'],
        ['02', 'Registered · active · normal', 'Not paused, not mid-setup', 'Locate sheet (G·01)', 'var(--text-primary)'],
        ['03', 'Registered · active · long-tap', 'User held button ≥ 2s, not paused', 'Hold sheet (G·02)', 'var(--accent-brown)'],
        ['04', 'Registered · paused', 'Currently in hold mode', 'Resume sheet (G·04)', 'var(--accent-green)'],
        ['05', 'Registered · reprogram flag', 'User chose "reassign" from Locate → Something\'s wrong', 'Reprogram flow (uses B4 subset)', 'var(--text-secondary)'],
      ].map(([n, head, sub, out, color]) => (
        <div key={n} style={{ display: 'grid', gridTemplateColumns: '36px 1fr 1fr', gap: 18, padding: '18px 0', borderTop: '1px solid var(--border)', alignItems: 'start' }}>
          <div style={{ fontFamily: 'var(--mono)', fontSize: 11, letterSpacing: '0.12em', color: 'var(--text-tertiary)' }}>{n}</div>
          <div>
            <div style={{ fontFamily: 'var(--serif)', fontSize: 17, fontWeight: 500 }}>{head}</div>
            <div className="muted" style={{ fontSize: 12.5, marginTop: 2 }}>{sub}</div>
          </div>
          <div style={{ fontFamily: 'var(--serif)', fontSize: 15, fontStyle: 'italic', color }}>
            → {out}
          </div>
        </div>
      ))}

      <div style={{ marginTop: 28, padding: 18, background: 'var(--bg-cream-lightest)', border: '1px solid var(--border)', borderRadius: 14 }}>
        <div className="caps" style={{ marginBottom: 6 }}>Principle</div>
        <div className="body-t" style={{ color: 'var(--text-primary)' }}>
          The serial answers <em>which</em>. The sheet answers <em>what next</em>. The identity card answers <em>recognition</em>. No radio lists, no map, no radar.
        </div>
      </div>
    </div>
    <div className="caption">
      <span className="id">G · 00</span>
      <span className="name">State diagram</span>
    </div>
  </div>
);

// Illustration set card
const G_Illustrations = () => (
  <div className="screen" style={{ gridColumn: 'span 1' }}>
    <div style={{ background: 'var(--bg-cream)', border: '1px solid var(--border)', borderRadius: 24, padding: 28, minHeight: 760, display: 'flex', flexDirection: 'column' }}>
      <div className="caps" style={{ color: 'var(--accent-brown)', marginBottom: 6 }}>New illustrations</div>
      <div className="h-l" style={{ fontSize: 24, marginBottom: 22 }}>A closed bud, and its opening.</div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 20, padding: 18, background: 'var(--bg-cream-lightest)', border: '1px solid var(--border)', borderRadius: 16 }}>
          <ClosedBud size={90}/>
          <div>
            <div style={{ fontFamily: 'var(--serif)', fontSize: 16, fontWeight: 500 }}>Closed bud</div>
            <div className="muted" style={{ fontSize: 12, marginTop: 2 }}>Used on Hold confirmation.</div>
          </div>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 20, padding: 18, background: 'var(--bg-cream-lightest)', border: '1px solid var(--border)', borderRadius: 16 }}>
          <OpeningLeaf size={90}/>
          <div>
            <div style={{ fontFamily: 'var(--serif)', fontSize: 16, fontWeight: 500 }}>Opening leaf</div>
            <div className="muted" style={{ fontSize: 12, marginTop: 2 }}>Used on Resume confirmation.</div>
          </div>
        </div>
        <div style={{ display: 'flex', alignItems: 'center', gap: 20, padding: 18, background: 'var(--bg-cream-lightest)', border: '1px solid var(--border)', borderRadius: 16 }}>
          <WateringCan size={90}/>
          <div>
            <div style={{ fontFamily: 'var(--serif)', fontSize: 16, fontWeight: 500 }}>Tilted can</div>
            <div className="muted" style={{ fontSize: 12, marginTop: 2 }}>A/B alternate for Hold.</div>
          </div>
        </div>
      </div>

      <div style={{ marginTop: 'auto', paddingTop: 24, fontFamily: 'var(--serif)', fontStyle: 'italic', fontSize: 13.5, color: 'var(--text-secondary)', lineHeight: 1.55 }}>
        The first and last frame of a tiny animation the app never plays — the user's hand is what moves between them.
      </div>
    </div>
    <div className="caption">
      <span className="id">G · Art</span>
      <span className="name">Illustration set</span>
    </div>
  </div>
);

Object.assign(window, {
  G1_Locate, G2_Hold, G3_HoldConfirm, G4_Resume, G5_ResumeConfirm,
  G_StatesDiagram, G_Illustrations,
  ClosedBud, OpeningLeaf, WateringCan,
});
