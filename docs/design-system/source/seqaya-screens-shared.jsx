// Phone frame, status bar, tab bar, and shared bits used by every screen file.

const Phone = ({ children, dark = false, noTabs = false, tab, style = {} }) => {
  return (
    <div className={"phone grain " + (dark ? "dark" : "")} style={style}>
      <div className="phone-inner">
        <StatusBar dark={dark} />
        <div className="content">
          {children}
        </div>
        {!noTabs && <TabBar active={tab} />}
      </div>
      <div className={"hp " + (dark ? "dark" : "")} />
    </div>
  );
};

const StatusBar = ({ dark }) => (
  <div className={"sb " + (dark ? "dark" : "")}>
    <span style={{ fontVariantNumeric: 'tabular-nums' }}>9:41</span>
    <div className="punch" />
    <div className="sb-icons">
      <svg width="14" height="10" viewBox="0 0 14 10" fill="currentColor"><path d="M1 7h2v3H1zM5 5h2v5H5zM9 3h2v7H9zM13 1h-2" stroke="currentColor" strokeWidth="1.3" fill="none" strokeLinecap="round"/><path d="M1 7h2v3H1zM5 5h2v5H5zM9 3h2v7H9z" fill="currentColor"/></svg>
      <svg width="14" height="10" viewBox="0 0 14 10" fill="none" stroke="currentColor" strokeWidth="1.3" strokeLinecap="round">
        <path d="M2 5l5-3 5 3M4 6.5l3-1.7 3 1.7M6 8l1-.5 1 .5"/>
      </svg>
      <svg width="20" height="10" viewBox="0 0 20 10" fill="none" stroke="currentColor" strokeWidth="1.1">
        <rect x="1" y="1.5" width="16" height="7" rx="1.5"/>
        <rect x="2.5" y="3" width="12" height="4" rx="0.5" fill="currentColor" stroke="none"/>
        <rect x="18" y="4" width="1.2" height="2" fill="currentColor" stroke="none"/>
      </svg>
    </div>
  </div>
);

const TabBar = ({ active = 'home' }) => (
  <div className="tabs">
    <div className={"tab " + (active === 'home' ? 'active' : '')}>
      {active === 'home' ? <I.homeFill size={20}/> : <I.home size={20}/>}
      <span className="label">Home</span>
    </div>
    <div className={"tab " + (active === 'scan' ? 'active' : '')}>
      {active === 'scan' ? <I.scanFill size={20}/> : <I.scan size={20}/>}
      <span className="label">Scan</span>
    </div>
    <div className={"tab " + (active === 'library' ? 'active' : '')}>
      {active === 'library' ? <I.bookFill size={20}/> : <I.book size={20}/>}
      <span className="label">Library</span>
    </div>
  </div>
);

// A labeled screen wrapper with the caption beneath
const Screen = ({ id, name, children }) => (
  <div className="screen">
    {children}
    <div className="caption">
      <span className="id">{id}</span>
      <span className="name">{name}</span>
    </div>
  </div>
);

// Top bar for internal screens
const TopBar = ({ left, center, right, dark }) => (
  <div className="topbar">
    <div style={{ width: 36, display: 'flex', justifyContent: 'flex-start' }}>{left}</div>
    <div style={{ flex: 1, textAlign: 'center' }}>{center}</div>
    <div style={{ width: 36, display: 'flex', justifyContent: 'flex-end' }}>{right}</div>
  </div>
);

// Step header for wizard
const StepHeader = ({ step, onBack = true, right = 'Skip for now' }) => (
  <TopBar
    left={onBack ? <I.back size={22}/> : <I.close size={22}/>}
    center={<span className="caps">Step {step} of 3</span>}
    right={<span style={{ fontFamily: 'var(--sans)', fontSize: 13, color: 'var(--accent-brown)', fontWeight: 500 }}>{right}</span>}
  />
);

Object.assign(window, { Phone, StatusBar, TabBar, Screen, TopBar, StepHeader });
