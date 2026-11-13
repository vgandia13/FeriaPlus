import { useState } from "react";
import { Link, useNavigate, useLocation } from "react-router-dom";
import { Menu, X } from "lucide-react";
import { Button } from "./ui/button";
import { ThemeToggle } from "./ThemeToggle";
import { useData } from "@/contexts/AppContext";

const Navbar = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { usuario, isLogged, setUsuario, setIsLogged } = useData();
  // Menú desplegable solo en móvil (por debajo de md)
  const [menuAbierto, setMenuAbierto] = useState(false);
  const cerrarMenu = () => setMenuAbierto(false);

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("usuario");
    setUsuario(null);
    setIsLogged(false);
    cerrarMenu();
    navigate("/login");
  };

  const isActive = (path: string) =>
    location.pathname === path
      ? "text-primary-foreground bg-white/15"
      : "text-primary-foreground/60 hover:text-primary-foreground hover:bg-white/10";

  const initials = usuario?.name
    ? usuario.name
        .split(" ")
        .map((n: string) => n[0])
        .join("")
        .slice(0, 2)
        .toUpperCase()
    : "?";

  const esAdmin = isLogged && usuario?.rol === "ROLE_ADMIN";
  const conSesion = isLogged && usuario;

  // Enlaces principales: los mismos en escritorio y en el menú móvil
  const enlaces = [
    { to: "/", texto: "Home" },
    { to: "/events", texto: "Eventos" },
    ...(esAdmin ? [{ to: "/admin", texto: "Admin" }] : []),
  ];

  return (
    <header className="relative z-50 bg-primary shadow-sm border-b border-white/10">
      <nav
        aria-label="Principal"
        className="h-15 flex items-center px-4 md:px-7 gap-4 md:gap-8"
      >
        {/* Logo */}
        <Link
          to="/"
          onClick={cerrarMenu}
          className="flex items-center gap-2 text-primary-foreground font-semibold text-[17px] tracking-tight shrink-0"
        >
          <span className="w-2 h-2 rounded-full bg-secondary inline-block" />
          Feria+
        </Link>

        {/* Links de navegación (escritorio) */}
        <div className="hidden md:flex items-center gap-1 flex-1">
          {enlaces.map((e) => (
            <Link
              key={e.to}
              to={e.to}
              className={`text-sm px-3 py-1.5 rounded-md transition-colors duration-150 flex items-center gap-1.5 ${isActive(e.to)}`}
            >
              {e.texto}
              {e.to === "/admin" && (
                <span className="text-[10px] font-medium bg-secondary/25 text-secondary-foreground px-1.5 py-0.5 rounded-full">
                  ADMIN
                </span>
              )}
            </Link>
          ))}
        </div>

        {/* Acciones */}
        <div className="flex items-center gap-2.5 shrink-0 ml-auto md:ml-0">
          <ThemeToggle />

          {/* Escritorio: sesión en línea */}
          <div className="hidden md:flex items-center gap-2.5">
            <div className="w-px h-5 bg-primary-foreground/20" />
            {conSesion ? (
              <>
                <Button
                  variant="ghost"
                  asChild
                  className="text-sm text-primary-foreground/70 hover:text-primary-foreground hover:bg-white/10 h-8 px-3"
                >
                  <Link to="/dashboard">Mi panel</Link>
                </Button>

                {/* Avatar con iniciales */}
                <div
                  className="w-8 h-8 rounded-full bg-secondary/30 border border-secondary/50 flex items-center justify-center text-[11px] font-semibold text-primary-foreground cursor-default select-none"
                  title={usuario.name}
                >
                  {initials}
                </div>

                <button
                  onClick={handleLogout}
                  className="text-[13px] text-primary-foreground/60 hover:text-primary-foreground transition-colors px-2"
                >
                  Salir
                </button>
              </>
            ) : (
              <>
                <Button
                  variant="ghost"
                  asChild
                  className="text-sm text-primary-foreground/70 hover:text-primary-foreground hover:bg-white/10 h-8 px-3"
                >
                  <Link to="/login">Iniciar sesión</Link>
                </Button>
                <Button
                  asChild
                  className="text-sm h-8 px-4 bg-secondary text-secondary-foreground hover:bg-secondary/85 font-medium border-0"
                >
                  <Link to="/register">Registrarse</Link>
                </Button>
              </>
            )}
          </div>

          {/* Móvil: botón del menú */}
          <button
            type="button"
            className="md:hidden inline-flex items-center justify-center size-11 -mr-2 rounded-md text-primary-foreground hover:bg-white/10 focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-secondary"
            aria-expanded={menuAbierto}
            aria-controls="menu-movil"
            aria-label={menuAbierto ? "Cerrar menú" : "Abrir menú"}
            onClick={() => setMenuAbierto((abierto) => !abierto)}
          >
            {menuAbierto ? <X className="size-6" /> : <Menu className="size-6" />}
          </button>
        </div>
      </nav>

      {/* Móvil: panel desplegable */}
      {menuAbierto && (
        <div
          id="menu-movil"
          className="md:hidden border-t border-white/10 px-4 pb-4 pt-2"
        >
          <ul className="flex flex-col">
            {enlaces.map((e) => (
              <li key={e.to}>
                <Link
                  to={e.to}
                  onClick={cerrarMenu}
                  aria-current={location.pathname === e.to ? "page" : undefined}
                  className={`flex items-center min-h-11 px-3 rounded-md text-base ${isActive(e.to)}`}
                >
                  {e.texto}
                </Link>
              </li>
            ))}
            {conSesion && (
              <li>
                <Link
                  to="/dashboard"
                  onClick={cerrarMenu}
                  aria-current={location.pathname === "/dashboard" ? "page" : undefined}
                  className={`flex items-center min-h-11 px-3 rounded-md text-base ${isActive("/dashboard")}`}
                >
                  Mi panel
                </Link>
              </li>
            )}
          </ul>

          <div className="mt-3 pt-3 border-t border-white/10">
            {conSesion ? (
              <div className="flex items-center justify-between gap-3 px-3">
                <span className="flex items-center gap-2.5 min-w-0 text-sm text-primary-foreground">
                  <span className="w-8 h-8 shrink-0 rounded-full bg-secondary/30 border border-secondary/50 flex items-center justify-center text-[11px] font-semibold">
                    {initials}
                  </span>
                  <span className="truncate">{usuario.name}</span>
                </span>
                <button
                  onClick={handleLogout}
                  className="min-h-11 px-3 text-sm text-primary-foreground/70 hover:text-primary-foreground"
                >
                  Salir
                </button>
              </div>
            ) : (
              <div className="grid grid-cols-2 gap-2">
                <Button
                  variant="ghost"
                  asChild
                  className="h-11 text-primary-foreground hover:bg-white/10 border border-white/20"
                >
                  <Link to="/login" onClick={cerrarMenu}>
                    Iniciar sesión
                  </Link>
                </Button>
                <Button
                  asChild
                  className="h-11 bg-secondary text-secondary-foreground hover:bg-secondary/85 font-medium border-0"
                >
                  <Link to="/register" onClick={cerrarMenu}>
                    Registrarse
                  </Link>
                </Button>
              </div>
            )}
          </div>
        </div>
      )}
    </header>
  );
};

export default Navbar;
