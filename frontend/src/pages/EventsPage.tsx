import { EventoService } from "../services/eventoService";
import { toast } from "sonner";
import { useEffect, useState } from "react";
import { EventoDTO } from "../types/EventoDTO";
import {
  Card,
  CardContent,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import {
  InputGroup,
  InputGroupInput,
  InputGroupButton,
} from "@/components/ui/input-group";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import {
  Pagination,
  PaginationNext,
  PaginationPrevious,
} from "@/components/ui/pagination";
import { ChevronsLeft, ChevronsRight } from "lucide-react";
import { Link } from "react-router-dom";

const EventsPage = () => {
  const [events, setEvents] = useState<EventoDTO[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [searchTerm, setSearchTerm] = useState("");
  const [debouncedSearchTerm, setDebouncedSearchTerm] = useState("");
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(false);

  const loadEvents = async (nombre: string, p: number) => {

    setLoading(true);
    try {
      const data = await EventoService.getAll({ nombre, page: p, size: 9 });
      return data;
    } catch (error) {
      console.error("Error al cargar eventos", error);
      toast.error("No se pudieron cargar los eventos.");
      return { content: [], page: { totalPages: 0, totalElements: 0 } };
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    const timerId = setTimeout(() => {
      setDebouncedSearchTerm(searchTerm);
      setPage(0);
    }, 500);

    return () => clearTimeout(timerId);
  }, [searchTerm]);

  useEffect(() => {
    let isMounted = true;

    const fetchData = async () => {
      const data = await loadEvents(debouncedSearchTerm, page);
      if (isMounted) {
        setEvents(data.content);
        setTotalPages(data.page.totalPages);
      }
    };

    fetchData();

    return () => {
      isMounted = false;
    };
  }, [debouncedSearchTerm, page]);

  const handleSearch = () => {
    setDebouncedSearchTerm(searchTerm);
    setPage(0);
  };

  return (
    <div className="container mx-auto px-4 py-8">
      <nav className="py-2 px-8 m-2">
        <InputGroup>
          <InputGroupInput
            placeholder="Buscar eventos"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
          <InputGroupButton asChild>
            <Button className="rounded-2xl p-3" onClick={handleSearch}>
              Buscar
            </Button>
          </InputGroupButton>
        </InputGroup>
      </nav>
      <h1 className="text-3xl font-bold mb-8 text-center">
        Eventos Disponibles
      </h1>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {loading
          ? Array.from({ length: 3 }).map((_, i) => (
              <Card key={i} className="overflow-hidden">
                <Skeleton className="aspect-video w-full" />
                <CardHeader>
                  <Skeleton className="h-6 w-3/4" />
                </CardHeader>
                <CardContent>
                  <Skeleton className="h-4 w-full mb-2" />
                  <Skeleton className="h-4 w-2/3" />
                </CardContent>
                <CardFooter>
                  <Skeleton className="h-4 w-1/2" />
                </CardFooter>
              </Card>
            ))
          : events.map((event) => (
              <Card
                key={event.id}
                className="overflow-hidden hover:shadow-lg transition-shadow duration-300"
              >
                <div className="aspect-video w-full overflow-hidden">
                  <img
                    src={event.imagenUrl}
                    alt={event.nombre}
                    className="w-full h-full object-cover"
                  />
                </div>
                <CardHeader>
                  <CardTitle className="text-xl">{event.nombre}</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="text-muted-foreground line-clamp-3">
                    {event.descripcion}
                  </p>
                </CardContent>
                <CardFooter className="flex flex-col items-start gap-2 text-sm text-muted-foreground pt-4 border-t">
                  <p>📍 {event.ubicacion?.nombre ?? "Sin Ubicación"}</p>
                  <p>📅 {new Date(event.fecha).toLocaleDateString()}</p>
                  <Link className="text-primary hover:underline" to={`/events/${event.id}`}>Ver más</Link>
                </CardFooter>
              </Card>
            ))}
        {events.length === 0 && !loading && (
          <p className="col-span-full flex items-center justify-center h-full text-center text-muted-foreground">
            No se encontraron eventos.
          </p>
        )}
      </div>

      <div className="mt-8">
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

export default EventsPage;
