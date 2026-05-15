import { Link, useNavigate, useLocation } from "react-router-dom";
import { Button } from "./ui/button";
import { ThemeToggle } from "./ThemeToggle";
import { useData } from "@/contexts/AppContext";

const Navbar = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { usuario, setUsuario, setIsLogged } = useData();

  const handleLogout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("usuario");
    setUsuario(null);
    setIsLogged(false);
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

  return (
    <nav className="h-15 bg-primary flex items-center px-7 gap-8 shadow-sm z-50 border-b border-white/10">
      {/* Logo */}
      <Link
        to="/"
        className="flex items-center gap-2 text-primary-foreground font-semibold text-[17px] tracking-tight shrink-0"
      >
        <span className="w-2 h-2 rounded-full bg-secondary inline-block" />
        Feria+
      </Link>

      {/* Links de navegación */}
      <div className="flex items-center gap-1 flex-1">
        <Link
          to="/"
          className={`text-sm px-3 py-1.5 rounded-md transition-colors duration-150 ${isActive("/")}`}
        >
          Home
        </Link>
        <Link
          to="/events"
          className={`text-sm px-3 py-1.5 rounded-md transition-colors duration-150 ${isActive("/events")}`}
        >
          Eventos
        </Link>
        {usuario?.rol === "ROLE_ADMIN" && (
          <Link
            to="/admin"
            className={`text-sm px-3 py-1.5 rounded-md transition-colors duration-150 flex items-center gap-1.5 ${isActive("/admin")}`}
          >
            Admin
            <span className="text-[10px] font-medium bg-secondary/25 text-secondary-foreground px-1.5 py-0.5 rounded-full">
              ADMIN
            </span>
          </Link>
        )}
      </div>

      {/* Acciones */}
      <div className="flex items-center gap-2.5 shrink-0">
        <ThemeToggle />
        <div className="w-px h-5 bg-primary-foreground/20" />

        {usuario ? (
          <>
            <Button
              variant="ghost"
              asChild
              className="text-sm text-primary-foreground/70 hover:text-primary-foreground hover:bg-white/10 h-8 px-3"
            >
              <Link to="/dashboard">Mi panel</Link>
            </Button>

            {/* Avatar con iniciales */}
            <div className="w-8 h-8 rounded-full bg-secondary/30 border border-secondary/50 flex items-center justify-center text-[11px] font-semibold text-primary-foreground cursor-default select-none">
              {initials}
            </div>

            <button
              onClick={handleLogout}
              className="text-[13px] text-primary-foreground/40 hover:text-primary-foreground/70 transition-colors px-2"
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
    </nav>
  );
};

export default Navbar;
