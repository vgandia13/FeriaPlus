import React, { useState, FormEvent } from "react";
import { UsuarioResponseDTO } from "@/types/UsuarioResponseDTO";
import { toast } from "sonner";
import { userService } from "@/services/userService";
import { useData } from "@/contexts/AppContext";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";

interface ProfileEditFormProps {
  BotonCancelar: React.ReactNode;
  user: UsuarioResponseDTO;
  onSuccess: () => void;
}

const ProfileEditForm = ({
  BotonCancelar,
  user,
  onSuccess,
}: ProfileEditFormProps) => {
  const { usuario, setUsuario } = useData();
  const [loading, setLoading] = useState(false);
  const [nombre, setNombre] = useState(user.nombre);
  const [email, setEmail] = useState(user.email);
  const [oldPassword, setOldPassword] = useState("");
  const [newPassword, setNewPassword] = useState("");

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    try {
      setLoading(true);
      // Las contraseñas vacías no se envían: el backend las valida con @Size(min = 8)
      await userService.updateProfile({
        nombre,
        email,
        oldPassword: newPassword ? oldPassword : undefined,
        newPassword: newPassword || undefined,
      });

      if (usuario) {
        const actualizado = { ...usuario, name: nombre, email };
        setUsuario(actualizado);
        localStorage.setItem("usuario", JSON.stringify(actualizado));
      }
      toast.success("Perfil actualizado correctamente.");
      setOldPassword("");
      setNewPassword("");
      onSuccess();
    } catch (error) {
      console.error(error);
      toast.error("No se pudo actualizar el perfil. Revisa la contraseña actual.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <form method="post" onSubmit={handleSubmit} className="flex flex-col gap-4">
      <div className="flex flex-col gap-2">
        <Label htmlFor="nombre">Nombre:</Label>
        <Input
          type="text"
          required
          id="nombre"
          name="nombre"
          value={nombre}
          onChange={(e) => setNombre(e.target.value)}
        />
      </div>
      <div className="flex flex-col gap-2">
        <Label htmlFor="email">Correo:</Label>
        <Input
          type="email"
          required
          id="email"
          name="email"
          autoComplete="email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
        />
      </div>
      <div className="flex flex-col gap-2">
        <Label htmlFor="newPassword">Nueva contraseña (opcional):</Label>
        <Input
          type="password"
          id="newPassword"
          name="newPassword"
          autoComplete="new-password"
          minLength={8}
          value={newPassword}
          onChange={(e) => setNewPassword(e.target.value)}
        />
      </div>
      <div className="flex flex-col gap-2">
        <Label htmlFor="oldPassword">Contraseña actual:</Label>
        <Input
          type="password"
          id="oldPassword"
          name="oldPassword"
          autoComplete="current-password"
          required={!!newPassword}
          disabled={!newPassword}
          value={oldPassword}
          onChange={(e) => setOldPassword(e.target.value)}
        />
      </div>
      <div className="flex gap-2">
        <Button type="submit" disabled={loading}>
          {loading ? "Guardando..." : "Guardar cambios"}
        </Button>
        {BotonCancelar}
      </div>
    </form>
  );
};

export default ProfileEditForm;
