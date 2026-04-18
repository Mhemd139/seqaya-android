// A1. Splash, A2. Sign In

const A1_Splash = () => (
  <Screen id="A · 01" name="Splash">
    <Phone noTabs>
      <div style={{ flex: 1, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', gap: 18 }}>
        <div style={{ fontFamily: 'var(--serif)', fontWeight: 500, fontSize: 38, letterSpacing: '-0.025em', fontVariationSettings: '"opsz" 144' }}>
          Seqaya
        </div>
        <LeafMark size={38} color="#788c5d" />
      </div>
      <div style={{ textAlign: 'center', paddingBottom: 28, fontFamily: 'var(--mono)', fontSize: 10, letterSpacing: '0.16em', textTransform: 'uppercase', color: 'var(--text-tertiary)' }}>
        v2.0 · windowsill edition
      </div>
    </Phone>
  </Screen>
);

const A2_SignIn = () => (
  <Screen id="A · 02" name="Sign in">
    <Phone noTabs>
      <div style={{ padding: '18px 20px 0' }}>
        <span className="wordmark wordmark-small">Seqaya</span>
      </div>
      <div style={{ flex: 1, padding: '0 28px', display: 'flex', flexDirection: 'column', justifyContent: 'center', gap: 24 }}>
        <div>
          <div className="h-xl" style={{ fontSize: 42 }}>Care,<br/>quietly.</div>
          <div className="body-t-lg muted" style={{ marginTop: 14, maxWidth: '28ch' }}>
            Seqaya keeps your plants watered while you live your life.
          </div>
        </div>
        <div>
          <div className="btn btn-primary">
            <svg width="18" height="18" viewBox="0 0 18 18"><path fill="#fff" d="M17.64 9.2c0-.64-.06-1.25-.17-1.84H9v3.48h4.84a4.14 4.14 0 01-1.8 2.72v2.26h2.92c1.7-1.57 2.68-3.88 2.68-6.62z"/><path fill="#faf9f5" d="M9 18c2.43 0 4.47-.8 5.96-2.18l-2.91-2.26c-.8.54-1.84.86-3.05.86-2.34 0-4.33-1.58-5.04-3.7H.9v2.34A9 9 0 009 18z"/><path fill="#faf9f5" d="M3.96 10.71A5.41 5.41 0 013.68 9c0-.59.1-1.17.28-1.71V4.96H.9A9 9 0 000 9c0 1.45.35 2.83.96 4.04l3-2.33z"/><path fill="#faf9f5" d="M9 3.58c1.32 0 2.5.45 3.44 1.35l2.58-2.59A9 9 0 00.9 4.96l3.04 2.33C4.67 5.16 6.66 3.58 9 3.58z"/></svg>
            Continue with Google
          </div>
          <div className="fine" style={{ textAlign: 'center', marginTop: 14 }}>
            By continuing you agree to our <u>Terms</u> and <u>Privacy</u>.
          </div>
        </div>
      </div>
      <div style={{ textAlign: 'center', padding: '0 24px 32px', fontFamily: 'var(--serif)', fontStyle: 'italic', fontSize: 13, color: 'var(--text-tertiary)' }}>
        Made for the windowsill, not the warehouse.
      </div>
    </Phone>
  </Screen>
);

Object.assign(window, { A1_Splash, A2_SignIn });
