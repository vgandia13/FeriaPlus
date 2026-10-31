import { AuthService } from "@/services/authService";
import { useState } from "react";
import { LoginRequest } from "@/types/LoginRequest";
import {
  Card,
  CardContent,
  CardDescription,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { toast } from "sonner";
import { FaRegEye, FaRegEyeSlash } from "react-icons/fa";
import { useData } from "@/contexts/AppContext";
import { useNavigate } from "react-router-dom";
import { Label } from "@/components/ui/label";

const LoginPage = () => {
  const { setUsuario, setIsLogged } = useData();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const navigate = useNavigate();
  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);

    const payload: LoginRequest = { email, password };

    try {
      const response = await AuthService.login(payload);
      localStorage.setItem("token", response.token);
      setUsuario(
        response.usuario as {
          name: string;
          email: string;
          rol: import("@/types/Rol").Rol;
        },
      );
      console.log("Login exitoso");
      toast("Login exitoso", {
        description: "Bienvenido a Feria+",
      });
      setIsLogged(true);
      navigate("/");
    } catch (error) {
      console.error("Error en las credenciales:", error);
      toast.error("Error en las credenciales", {
        description: "Por favor, revisa el correo y la contraseña",
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
            Introduce tu email y contraseña para acceder a tu cuenta
          </CardDescription>
        </CardHeader>
        <form onSubmit={handleLogin}>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="email" id="email" className="text-sm font-medium">
                Email
              </Label>
              <Input
                type="email"
                placeholder="nombre@ejemplo.com"
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
              <div className="relative flex items-center pb-2 py-2 mb-2">
                <Input
                  type={showPassword ? "text" : "password"}
                  value={password}
                  placeholder="Introduce tu contraseña"
                  onChange={(e) => setPassword(e.target.value)}
                  className="pr-10"
                  required
                  autoComplete="current-password"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  aria-label={
                    showPassword ? "Ocultar contraseña" : "Mostrar contraseña"
                  }
                  aria-pressed={showPassword}
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
          </CardContent>
          <CardFooter>
            <Button type="submit" className="w-full" disabled={isLoading}>
              {isLoading ? "Cargando..." : "Iniciar sesión"}
            </Button>
          </CardFooter>
        </form>
      </Card>
    </div>
  );
};

export default LoginPage;
