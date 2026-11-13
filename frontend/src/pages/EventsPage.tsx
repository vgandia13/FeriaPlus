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
import { Link } from "react-router-dom";
import { Paginacion } from "@/components/Paginacion";
import { formatDate } from "@/lib/fechas";
import { Badge } from "@/components/ui/badge";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { CategoriaService } from "../services/categoriaService";
import { CategoriaDTO } from "../types/CategoriaDTO";

// Valor del desplegable para "sin filtro": Radix Select no admite value=""
const TODAS = "todas";
const RADIOS_KM = ["10", "25", "50", "100"];

interface Posicion {
  lat: number;
  lng: number;
}

const EventsPage = () => {
  const [events, setEvents] = useState<EventoDTO[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [searchTerm, setSearchTerm] = useState("");
  const [debouncedSearchTerm, setDebouncedSearchTerm] = useState("");
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(false);
  const [categorias, setCategorias] = useState<CategoriaDTO[]>([]);
  const [categoria, setCategoria] = useState(TODAS);
  const [posicion, setPosicion] = useState<Posicion | null>(null);
  const [radioKm, setRadioKm] = useState("25");
  const [localizando, setLocalizando] = useState(false);

  const loadEvents = async (
    nombre: string,
    cat: string,
    pos: Posicion | null,
    radio: string,
    p: number,
  ) => {
    setLoading(true);
    try {
      const data = await EventoService.getAll({
        nombre,
        categoriaId: cat === TODAS ? undefined : Number(cat),
        lat: pos?.lat,
        lng: pos?.lng,
        radioKm: pos ? Number(radio) : undefined,
        page: p,
        size: 9,
      });
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
      const data = await loadEvents(
        debouncedSearchTerm,
        categoria,
        posicion,
        radioKm,
        page,
      );
      if (isMounted) {
        setEvents(data.content);
        setTotalPages(data.page.totalPages);
      }
    };

    fetchData();

    return () => {
      isMounted = false;
    };
  }, [debouncedSearchTerm, categoria, posicion, radioKm, page]);

  // Las categorías se cargan una vez; si fallan, la página sigue funcionando sin el filtro
  useEffect(() => {
    CategoriaService.getAll()
      .then(setCategorias)
      .catch((error) => console.error("Error al cargar categorías", error));
  }, []);

  const nombreCategoria = (id?: number) =>
    categorias.find((c) => c.id === id)?.nombre;

  // Pide la ubicación al navegador (solo funciona en localhost o HTTPS).
  // Si ya estaba activo, lo desactiva.
  const toggleCercaDeMi = () => {
    if (posicion) {
      setPosicion(null);
      setPage(0);
      return;
    }
    if (!navigator.geolocation) {
      toast.error("Tu navegador no permite obtener la ubicación.");
      return;
    }
    setLocalizando(true);
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        setPosicion({ lat: pos.coords.latitude, lng: pos.coords.longitude });
        setPage(0);
        setLocalizando(false);
      },
      () => {
        toast.error(
          "No se pudo obtener tu ubicación. Revisa el permiso del navegador.",
        );
        setLocalizando(false);
      },
      { timeout: 10000 },
    );
  };

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
        <div className="mt-3 flex flex-wrap items-center gap-3">
          <Select
            value={categoria}
            onValueChange={(valor) => {
              setCategoria(valor);
              setPage(0);
            }}
          >
            <SelectTrigger
              className="w-full sm:w-60"
              aria-label="Filtrar por categoría"
            >
              <SelectValue placeholder="Categoría" />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value={TODAS}>Todas las categorías</SelectItem>
              {categorias.map((c) => (
                <SelectItem key={c.id} value={String(c.id)}>
                  {c.nombre}
                </SelectItem>
              ))}
            </SelectContent>
          </Select>
          <Button
            type="button"
            variant={posicion ? "default" : "outline"}
            aria-pressed={posicion !== null}
            disabled={localizando}
            onClick={toggleCercaDeMi}
          >
            {localizando
              ? "Localizando…"
              : posicion
                ? "Quitar «Cerca de mí»"
                : "📍 Cerca de mí"}
          </Button>
          {posicion && (
            <Select
              value={radioKm}
              onValueChange={(valor) => {
                setRadioKm(valor);
                setPage(0);
              }}
            >
              <SelectTrigger className="w-32" aria-label="Distancia máxima">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {RADIOS_KM.map((r) => (
                  <SelectItem key={r} value={r}>
                    {r} km
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          )}
        </div>
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
                  {nombreCategoria(event.categoriaId) && (
                    <Badge variant="secondary" className="w-fit">
                      {nombreCategoria(event.categoriaId)}
                    </Badge>
                  )}
                  <CardTitle className="text-xl">{event.nombre}</CardTitle>
                </CardHeader>
                <CardContent>
                  <p className="text-muted-foreground line-clamp-3">
                    {event.descripcion}
                  </p>
                </CardContent>
                <CardFooter className="flex flex-col items-start gap-2 text-sm text-muted-foreground pt-4 border-t">
                  <p>📍 {event.ubicacion?.nombre ?? "Sin Ubicación"}</p>
                  <p>📅 {formatDate(event.fecha)}</p>
                  <Link
                    className="text-primary hover:underline"
                    to={`/events/${event.id}`}
                  >
                    Ver más
                  </Link>
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
        <Paginacion
          page={page}
          totalPages={totalPages}
          onPageChange={setPage}
        />
      </div>
    </div>
  );
};

export default EventsPage;
