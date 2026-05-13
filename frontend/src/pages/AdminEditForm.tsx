import React, { useState, useEffect, FormEvent } from "react";
import { UsuarioResponseDTO } from "@/types/UsuarioResponseDTO";
import { toast } from "sonner";
import { adminService } from "@/services/adminService";
import { Skeleton } from "@/components/ui/skeleton";
import { Label } from "@/components/ui/label";
import { Rol } from "@/types/Rol";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";

interface AdminEditFormProps {
  BotonCancelar: React.ReactNode;
  user: UsuarioResponseDTO;
  onSuccess: () => void;
}

const AdminEditForm = ({
  BotonCancelar,
  user,
  onSuccess,
}: AdminEditFormProps) => {
  const [loading, setLoading] = useState(false);
  const [updateUser, setUpdateUser] =
    useState<Partial<UsuarioResponseDTO>>(user);

  useEffect(() => {
    //eslint-disable-next-line
    setUpdateUser(user);
  }, [user]);

  const handleSubmit = async (e: FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    if (!updateUser) {
      toast.error("No hay datos de usuario para actualizar.");
      return;
    }

    try {
      setLoading(true);
      await adminService.updateUser(updateUser as UsuarioResponseDTO);
      toast.success("Usuario actualizado correctamente.");
      onSuccess();
    } catch (error) {
      console.error(error);
      toast.error("No se pudo actualizar el usuario.");
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <Skeleton className="h-4 w-1/4" />;
  }

  return (
    <form method="post" onSubmit={handleSubmit}>
      <div className="flex flex-col gap-2">
        <Label htmlFor="name">Nombre:</Label>
        <Input
          type="text"
          id="name"
          name="name"
          value={updateUser?.nombre || ""}
          onChange={(e) =>
            setUpdateUser({ ...updateUser!, nombre: e.target.value })
          }
        />
      </div>
      <div className="flex flex-col gap-2">
        <Label htmlFor="email">Correo:</Label>
        <Input
          type="email"
          id="email"
          name="email"
          value={updateUser?.email || ""}
          onChange={(e) =>
            setUpdateUser({ ...updateUser!, email: e.target.value })
          }
        />
      </div>
      <div className="flex flex-col gap-2 my-4">
        <Label htmlFor="rol">Rol:</Label>
        <Select
          value={updateUser?.rol || ""}
          onValueChange={(value) =>
            setUpdateUser({ ...updateUser!, rol: value as Rol })
          }
        >
          <SelectTrigger>
            <SelectValue placeholder="Seleccione un rol" />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="ROLE_VISITANTE">Visitante</SelectItem>
            <SelectItem value="ROLE_ADMIN">Administrador</SelectItem>
            <SelectItem value="ROLE_ORGANIZADOR">Organizador</SelectItem>
            <SelectItem value="ROLE_EXPOSITOR">Expositor</SelectItem>
          </SelectContent>
        </Select>
      </div>
      <Button type="submit">Guardar cambios</Button>
      <Button type="reset" variant={"secondary"}>
        Limpiar
      </Button>
      {BotonCancelar}
    </form>
  );
};

export default AdminEditForm;
