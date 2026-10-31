import { useState } from "react";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { toast } from "sonner";
import { AuthService } from "@/services/authService";
import { UsuarioRegistro } from "@/types/UsuarioRegistro";
import { FaRegEye, FaRegEyeSlash } from "react-icons/fa";
import { Rol } from "@/types/Rol";
import { useNavigate } from "react-router-dom";
import { Label } from "@/components/ui/label";

const RegisterPage = () => {
  const navigate = useNavigate();

  const [username, setUsername] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [rol, setRol] = useState("ROLE_VISITANTE");

  const handleRegister = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);

    if (password !== confirmPassword) {
      toast.error("Las contraseñas no coinciden");
      setIsLoading(false);
      return;
    }

    const payload: UsuarioRegistro = {
      nombre: username,
      email,
      password,
      rol: rol as Rol,
    };

    try {
      await AuthService.register(payload);
      console.log("Registro exitoso");
      toast("Registro exitoso", {
        description: "Bienvenido a Feria+",
      });

      navigate("/login");
    } catch (error) {
      console.error("Error en el registro:", error);
      toast.error("Error en el registro", {
        description: "Compruebe los campos e intente nuevamente",
      });
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="flex items-center justify-center min-h-screen">
      <Card className="w-full max-w-md">
        <CardHeader className="space-y-1">
          <CardTitle className="text-2xl font-bold">Feria+</CardTitle>
          <CardDescription>
            Crea tu cuenta para acceder a todas las funcionalidades de Feria+
          </CardDescription>
        </CardHeader>
        <form onSubmit={handleRegister}>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label
                htmlFor="nombre"
                id="nombre"
                className="text-sm font-medium"
              >
                Nombre de Usuario
              </Label>
              <Input
                type="text"
                placeholder="Tu nombre de usuario"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                required
                autoComplete="username"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="email" id="email" className="text-sm font-medium">
                Email
              </Label>
              <Input
                type="email"
                placeholder="Tu email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                autoComplete="email"
              />
            </div>
            <div className="space-y-2">
              <Label
                htmlFor="password"
                id="password"
                className="text-sm font-medium"
              >
                Contraseña
              </Label>
              <div className="relative flex items-center pb-2 py-2">
                <Input
                  type={showPassword ? "text" : "password"}
                  value={password}
                  placeholder="Introduce tu contraseña (mínimo 8 caracteres)"
                  onChange={(e) => setPassword(e.target.value)}
                  required
                  autoComplete="new-password"
                />
                <button
                  type="button"
                  aria-label={
                    showPassword ? "Ocultar contraseña" : "Mostrar contraseña"
                  }
                  aria-pressed={showPassword}
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-zinc-700 hover:text-zinc-900 transition-colors rounded focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
                >
                  {showPassword ? (
                    <FaRegEyeSlash size={18} />
                  ) : (
                    <FaRegEye size={18} />
                  )}
                </button>
              </div>
            </div>
            <div className="space-y-2">
              <Label
                htmlFor="confirmPassword"
                id="confirmPassword"
                className="text-sm font-medium"
              >
                Confirma tu contraseña
              </Label>
              <div className="relative flex items-center pb-2 py-2">
                <Input
                  type={showConfirmPassword ? "text" : "password"}
                  value={confirmPassword}
                  placeholder="Introduce tu contraseña"
                  onChange={(e) => setConfirmPassword(e.target.value)}
                  className="pr-10"
                  required
                  autoComplete="new-password"
                />
                <button
                  type="button"
                  aria-label={
                    showConfirmPassword
                      ? "Ocultar contraseña"
                      : "Mostrar contraseña"
                  }
                  aria-pressed={showConfirmPassword}
                  onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-zinc-700 hover:text-zinc-900 transition-colors rounded focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring"
                >
                  {showConfirmPassword ? (
                    <FaRegEyeSlash size={18} />
                  ) : (
                    <FaRegEye size={18} />
                  )}
                </button>
              </div>
            </div>
            <div className="space-y-2">
              <Label htmlFor="rol" className="text-sm font-medium">
                Rol
              </Label>
              <Select value={rol} onValueChange={setRol}>
                <SelectTrigger id="rol" className="w-full">
                  <SelectValue placeholder="Selecciona un rol" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="ROLE_VISITANTE">Visitante</SelectItem>
                  <SelectItem value="ROLE_EXPOSITOR">Expositor</SelectItem>
                  <SelectItem value="ROLE_ORGANIZADOR">Organizador</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <Button type="submit" className="w-full" disabled={isLoading}>
              {isLoading ? "Registrando..." : "Registrarse"}
            </Button>
          </CardContent>
        </form>
      </Card>
    </div>
  );
};

export default RegisterPage;
