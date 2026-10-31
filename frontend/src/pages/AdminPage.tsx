import {
  Table,
  TableHead,
  TableHeader,
  TableRow,
  TableBody,
  TableCell,
} from "@/components/ui/table";
import { Skeleton } from "@/components/ui/skeleton";
import { toast } from "sonner";
import { adminService } from "@/services/adminService";
import { useEffect, useState } from "react";
import { UsuarioResponseDTO } from "@/types/UsuarioResponseDTO";
import { Button } from "@/components/ui/button";
import {
  InputGroup,
  InputGroupButton,
  InputGroupInput,
} from "@/components/ui/input-group";
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import AdminEditForm from "./AdminEditForm";
import { Paginacion } from "@/components/Paginacion";
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

const ROL_LABELS: Record<string, string> = {
  ROLE_ADMIN: "Admin",
  ROLE_ORGANIZADOR: "Organizador",
  ROLE_VISITANTE: "Visitante",
  ROLE_EXPOSITOR: "Expositor",
};

export const formatDate = (ISOdate: string) =>
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
  const [searchTerms, setSearchTerms] = useState("");
  const [debouncedSearchTerms, setDebouncedSearchTerms] = useState("");
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [editando, setEditando] = useState<UsuarioResponseDTO | null>(null);

  const loadUsers = async (nombre: string, p: number) => {
    setLoading(true);
    try {
      const data = await adminService.getAllUsers({
        nombre,
        page: p,
        size: 10,
      });
      return data;
    } catch (error) {
      console.error("Error al cargar usuarios", error);
      toast.error("No se pudieron cargar los usuarios.");
      return { content: [], page: { totalPages: 0, totalElements: 0 } };
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    const timerId = setTimeout(() => {
      setDebouncedSearchTerms(searchTerms);
      setPage(0);
    }, 500);

    return () => clearTimeout(timerId);
  }, [searchTerms]);

  useEffect(() => {
    let isMounted = true;
    const fetchData = async () => {
      const data = await loadUsers(debouncedSearchTerms, page);
      if (isMounted) {
        setUsers(data.content);
        setTotalPages(data.page.totalPages);
        setTotalElements(data.page.totalElements);
      }
    };

    fetchData();

    return () => {
      isMounted = false;
    };
  }, [debouncedSearchTerms, page]);

  const handleSearch = () => {
    setDebouncedSearchTerms(searchTerms);
    setPage(0);
  };

  const handleDelete = async (id: number) => {
    try {
      await adminService.deleteUser(id);
      toast.success("Usuario eliminado correctamente.");
      const dataFetch = await loadUsers(debouncedSearchTerms, page);
      const nuevasPaginas = dataFetch.page.totalPages;
      if (page > 0 && page >= nuevasPaginas) {
        setPage(Math.max(0, nuevasPaginas - 1));
        return;
      }
      setUsers(dataFetch.content);
      setTotalPages(nuevasPaginas);
      setTotalElements(dataFetch.page.totalElements);
    } catch (error) {
      console.error(error);
      toast.error("No se pudo eliminar el usuario.");
    }
  };

  return (
    <div className="container mx-auto px-4 py-8">
      <h1 className="text-3xl font-bold mb-2">Panel de Administración</h1>
      <p className="text-muted-foreground mb-4">
        Gestiona los usuarios registrados en la plataforma.
      </p>
      <p className="text-muted-foreground mb-6">
        Usuarios totales: {totalElements}
      </p>

      <div className="overflow-x-auto rounded-lg border">
        <nav className="py-2 px-8 m-2">
          <InputGroup>
            <InputGroupInput
              placeholder="Buscar usuarios"
              value={searchTerms}
              onChange={(e) => setSearchTerms(e.target.value)}
            />
            <InputGroupButton asChild>
              <Button className="rounded-2xl p-3" onClick={handleSearch}>
                Buscar
              </Button>
            </InputGroupButton>
          </InputGroup>
        </nav>

        <Table>
          <TableHeader>
            <TableRow>
              <TableHead className="border-r border-border text-center">
                Id
              </TableHead>
              <TableHead className="border-r border-border text-center">
                Nombre
              </TableHead>
              <TableHead className="border-r border-border text-center">
                Email
              </TableHead>
              <TableHead className="border-r border-border text-center">
                Rol
              </TableHead>
              <TableHead className="border-r border-border text-center">
                Fecha de Registro
              </TableHead>
              <TableHead className="text-center">Acciones</TableHead>
            </TableRow>
          </TableHeader>
          <TableBody>
            {loading ? (
              Array.from({ length: 5 }).map((_, i) => (
                <TableRow key={i}>
                  {Array.from({ length: 6 }).map((_, j) => (
                    <TableCell
                      key={j}
                      className={j < 5 ? "border-r border-border" : ""}
                    >
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
                  <TableCell className="border-r border-border text-center">
                    {user.id}
                  </TableCell>
                  <TableCell className="border-r border-border text-center">
                    {user.nombre}
                  </TableCell>
                  <TableCell className="border-r border-border text-center">
                    {user.email}
                  </TableCell>
                  <TableCell className="border-r border-border text-center">
                    {ROL_LABELS[user.rol] ?? user.rol}
                  </TableCell>
                  <TableCell className="border-r border-border text-center">
                    {formatDate(user.fechaRegistro)}
                  </TableCell>
                  <TableCell className="text-center">
                    <AlertDialog>
                      <AlertDialogTrigger asChild>
                        <Button variant="destructive">Eliminar</Button>
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
                    <Button
                      variant="secondary"
                      className="ml-2"
                      onClick={() => setEditando(user)}
                    >
                      Editar
                    </Button>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      <div className="flex justify-end mt-8">
        <Paginacion
          page={page}
          totalPages={totalPages}
          onPageChange={setPage}
        />
      </div>
      <Dialog
        open={editando !== null}
        onOpenChange={(abierto) => !abierto && setEditando(null)}
      >
        <DialogContent className="w-auto">
          <DialogHeader>
            <DialogTitle>Editar usuario</DialogTitle>
            <DialogDescription>
              Cambia el nombre, el correo o el rol del usuario.
            </DialogDescription>
          </DialogHeader>
          {editando && (
            <AdminEditForm
              user={editando}
              onSuccess={async () => {
                setEditando(null);
                const data = await loadUsers(debouncedSearchTerms, page);
                setUsers(data.content);
                setTotalPages(data.page.totalPages);
                setTotalElements(data.page.totalElements);
              }}
              BotonCancelar={
                <DialogClose asChild>
                  <Button type="button">Cancelar</Button>
                </DialogClose>
              }
            />
          )}
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default AdminPage;
