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
import { useEffect, useState } from "react";
import { UsuarioResponseDTO } from "@/types/UsuarioResponseDTO";
import { Button } from "@/components/ui/button";
import {
  Pagination,
  PaginationNext,
  PaginationPrevious,
} from "@/components/ui/pagination";
import { ChevronsLeft, ChevronsRight } from "lucide-react";
import {
  InputGroup,
  InputGroupButton,
  InputGroupInput,
} from "@/components/ui/input-group";
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import AdminEditForm from "./AdminEditForm";

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
  const [totalElements, setTotalElements] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [searchTerms, setSearchTerms] = useState("");
  const [debouncedSearchTerms, setDebouncedSearchTerms] = useState("");
  const [page, setPage] = useState(0);

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
      return { content: [], totalPages: 0, totalElements: 0 };
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
        setTotalPages(data.totalPages);
        setTotalElements(data.totalElements);
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

  const handleDelete = async (id: string) => {
    try {
      await adminService.deleteUser(id);
      toast.success("Usuario eliminado correctamente.");
      const data = await loadUsers(debouncedSearchTerms, page);
      setUsers(data.content);
      setTotalPages(data.totalPages);
      setTotalElements(data.totalElements);
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
                    <Dialog>
                      <DialogTrigger asChild>
                        <Button variant="secondary" className="ml-2">
                          Editar
                        </Button>
                      </DialogTrigger>
                      <DialogContent aria-describedby="Formulario de edicion de usuario" className="w-auto">
                        <DialogHeader>
                          <DialogTitle>Editar Usuario</DialogTitle>
                        </DialogHeader>
                        <AdminEditForm
                          user={user}
                          onSuccess={async () => {
                            const data = await loadUsers(debouncedSearchTerms, page);
                            setUsers(data.content);
                            setTotalPages(data.totalPages);
                            setTotalElements(data.totalElements);
                            // Cierra el diálogo programáticamente si fuera necesario,
                            // pero DialogClose en el BotonCancelar ya debería manejarlo.
                          }}
                          BotonCancelar={
                            <DialogClose asChild>
                              <Button>Cancelar</Button>
                            </DialogClose>
                          }
                        />
                      </DialogContent>
                    </Dialog>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </div>

      <div className="flex justify-end mt-8">
        <Pagination className="flex items-center align-middle justify-center">
          <Button variant={"ghost"} onClick={() => setPage(0)}>
            <ChevronsLeft
              className={
                page === 0 ? "pointer-events-none opacity-50" : "cursor-pointer"
              }
            />
          </Button>
          <PaginationPrevious
            className={
              page === 0 ? "pointer-events-none opacity-50" : "cursor-pointer"
            }
            onClick={() => setPage((p) => Math.max(0, p - 1))}
          />
          <span className="mx-4 flex items-center text-sm font-medium">
            Página {page + 1} de {totalPages || 1}
          </span>
          <PaginationNext
            className={
              page >= totalPages - 1
                ? "pointer-events-none opacity-50"
                : "cursor-pointer"
            }
            onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
          />
          <Button variant={"ghost"}>
            <ChevronsRight
              onClick={() => setPage(totalPages - 1)}
              className={
                page >= totalPages - 1 ? "pointer-events-none opacity-50" : "cursor-pointer"
              }
            />
          </Button>
        </Pagination>
      </div>
    </div>
  );
};

export default AdminPage;
