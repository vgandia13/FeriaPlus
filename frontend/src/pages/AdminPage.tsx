import {
  Table,
  TableHead,
  TableHeader,
  TableRow,
  TableBody,
  TableCell,
} from "@/components/ui/table";
import { toast } from "sonner";
import { adminService } from "@/services/adminService";
import { useCallback, useEffect, useState } from "react";
import { UsuarioResponseDTO } from "@/types/UsuarioResponseDTO";
import { Button } from "@/components/ui/button";

const AdminPage = () => {
  const [users, setUsers] = useState<UsuarioResponseDTO[]>([]);

  const loadUsers = useCallback(async () => {
    try {
      const response = await adminService.getAllUsers();
      setUsers(response);
    } catch (error) {
      console.log(error);
      toast.error("No se pudieron cargar los usuarios.");
    }
  }, []);

  const handleDelete = async (id: string) => {
    try {
      await adminService.deleteUser(id);
      toast.success("Usuario eliminado correctamente.");
      loadUsers();
    } catch (error) {
      console.error(error);
      toast.error("No se pudo eliminar el usuario.");
    }
  };

  useEffect(() => {
    loadUsers();
  }, [loadUsers]);

  return (
    <div>
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Id</TableHead>
            <TableHead>Nombre</TableHead>
            <TableHead>email</TableHead>
            <TableHead>Rol</TableHead>
            <TableHead>Fecha de Registro</TableHead>
            <TableHead>Acciones</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {users.map((user) => (
            <TableRow key={user.id}>
              <TableCell>{user.id}</TableCell>
              <TableCell>{user.nombre}</TableCell>
              <TableCell>{user.email}</TableCell>
              <TableCell>{user.rol}</TableCell>
              <TableCell>{user.fechaRegistro}</TableCell>
              <TableCell>
                <Button onClick={() => handleDelete(user.id)}>Eliminar</Button>
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </div>
  );
};

export default AdminPage;
