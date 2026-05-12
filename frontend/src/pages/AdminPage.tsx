import {
  Table,
  TableHead,
  TableHeader,
  TableRow,
  TableBody,
  TableCell,
} from "@/components/ui/table";
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
  AlertDialogTrigger,
} from "@/components/ui/alert-dialog";
import { Skeleton } from "@/components/ui/skeleton";
import { toast } from "sonner";
import { adminService } from "@/services/adminService";
import { useCallback, useEffect, useState } from "react";
import { UsuarioResponseDTO } from "@/types/UsuarioResponseDTO";
import { Button } from "@/components/ui/button";

const ROL_LABELS: Record<string, string> = {
  ROLE_ADMIN: "Admin",
  ROLE_ORGANIZADOR: "Organizador",
  ROLE_VISITANTE: "Visitante",
  ROLE_EXPOSITOR: "Expositor",
};

const formatDate = (ISOdate: string) =>
  new Date(ISOdate).toLocaleDateString("es-ES", {
    day: "2-digit",
    month: "2-digit",
    year: "numeric",
    hour: "2-digit",
    minute: "2-digit",
  });

const AdminPage = () => {
  const [users, setUsers] = useState<UsuarioResponseDTO[]>([]);
  const [loading, setLoading] = useState(false);

  const loadUsers = useCallback(async () => {
    setLoading(true);
    try {
      const response = await adminService.getAllUsers();
      setUsers(response);
    } catch (error) {
      console.error(error);
      toast.error("No se pudieron cargar los usuarios.");
    } finally {
      setLoading(false);
    }
  }, []);

  const handleDelete = async (id: string) => {
    try {
      await adminService.deleteUser(id);
      toast.success("Usuario eliminado correctamente.");
      await loadUsers();
    } catch (error) {
      console.error(error);
      toast.error("No se pudo eliminar el usuario.");
    }
  };

  useEffect(() => {
    loadUsers();
  }, [loadUsers]);

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-2">Panel de Administración</h1>
      <p className="text-muted-foreground mb-6">
        Gestiona los usuarios registrados en la plataforma.
      </p>

      <div className="overflow-x-auto rounded-lg border">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Id</TableHead>
              <TableHead>Nombre</TableHead>
              <TableHead>Email</TableHead>
              <TableHead>Rol</TableHead>
              <TableHead>Fecha de Registro</TableHead>
              <TableHead>Acciones</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {loading ? (
              Array.from({ length: 5 }).map((_, i) => (
                <TableRow key={i}>
                  {Array.from({ length: 6 }).map((_, j) => (
                    <TableCell key={j}>
                      <Skeleton className="h-4 w-full" />
                    </TableCell>
                  ))}
                </TableRow>
              ))
            ) : users.length === 0 ? (
              <TableRow>
                <TableCell
                  colSpan={6}
                  className="text-center text-muted-foreground py-12"
                >
                  No hay usuarios registrados.
                </TableCell>
              </TableRow>
            ) : (
              users.map((user) => (
                <TableRow key={user.id}>
                  <TableCell>{user.id}</TableCell>
                  <TableCell>{user.nombre}</TableCell>
                  <TableCell>{user.email}</TableCell>
                  <TableCell>{ROL_LABELS[user.rol] ?? user.rol}</TableCell>
                  <TableCell>{formatDate(user.fechaRegistro)}</TableCell>
                  <TableCell>
                    <AlertDialog>
                      <AlertDialogTrigger asChild>
                        <Button variant="destructive" size="sm">
                          Eliminar
                        </Button>
                      </AlertDialogTrigger>
                      <AlertDialogContent className="w-auto">
                        <AlertDialogHeader>
                          <AlertDialogTitle>
                            ¿Eliminar usuario?
                          </AlertDialogTitle>
                          <AlertDialogDescription>
                            Esta acción no se puede deshacer. Se eliminará
                            permanentemente la cuenta de{" "}
                            <strong>{user.nombre}</strong>.
                          </AlertDialogDescription>
                        </AlertDialogHeader>
                        <AlertDialogFooter>
                          <AlertDialogCancel>Cancelar</AlertDialogCancel>
                          <AlertDialogAction
                            onClick={() => handleDelete(user.id)}
                          >
                            Confirmar
                          </AlertDialogAction>
                        </AlertDialogFooter>
                      </AlertDialogContent>
                    </AlertDialog>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>
    </div>
  );
};

export default AdminPage;
