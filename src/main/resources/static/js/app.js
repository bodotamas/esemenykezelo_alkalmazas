(() => {
  // Aktív menüpont kiemelés az aktuális URL alapján
  const path = window.location.pathname;
  document.querySelectorAll('.app-navbar a.nav-link[data-match]').forEach(a => {
    const match = a.getAttribute('data-match');
    if (match === "/" && path === "/") a.classList.add("active");
    else if (match !== "/" && path.startsWith(match)) a.classList.add("active");
  });
})();
