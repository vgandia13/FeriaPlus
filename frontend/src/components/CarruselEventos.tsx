import {
  Carousel,
  CarouselContent,
  CarouselItem,
  CarouselNext,
  CarouselPrevious,
} from "@/components/ui/carousel";
import {
  Card,
  CardContent,
  CardFooter,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { EventoDTO } from "@/types/EventoDTO";
import { useEffect, useState } from "react";
import { EventoService } from "@/services/eventoService";
import { toast } from "sonner";
import { Link } from "react-router-dom";
import { Button } from "./ui/button";

const CarruselEventos = () => {
  const [eventos, setEventos] = useState<EventoDTO[]>([]);

  useEffect(() => {
    const loadEventos = async () => {
      try {
        const response = await EventoService.getAll();

        setEventos(response.content.slice(0, 5));
      } catch (error) {
        console.error("Error de red al cargar eventos destacados", error);
        toast.error(
          "No se pudieron cargar los eventos destacados. Por favor, inténtalo de nuevo más tarde.",
        );
      }
    };

    loadEventos();
  }, []);

  if (eventos.length === 0) {
    return (
      <section className="w-full py-12 text-center">
        <h2 className="text-2xl font-bold mb-4">Eventos Destacados</h2>
        <p className="text-muted-foreground">
          Actualmente no hay eventos disponibles.
        </p>
      </section>
    );
  }

  return (
    <section className="w-full py-12">
      <h2 className="text-2xl font-bold mb-8 text-center">
        Eventos Destacados
      </h2>

      <Carousel
        opts={{
          align: "start",
          loop: true,
        }}
        className="w-full max-w-5xl mx-auto"
      >
        <CarouselContent className="-ml-4">
          {eventos.map((evento) => (
            <CarouselItem
              key={evento.id}
              className="pl-4 md:basis-1/2 lg:basis-1/3 flex"
            >
              <div className="p-1 w-full flex">
                <Card className="flex flex-col w-full h-full overflow-hidden border-none shadow-md hover:shadow-lg transition-shadow">
                  <div className="relative w-full aspect-video bg-muted shrink-0 overflow-hidden">
                    <img
                      src={evento.imagenUrl}
                      alt={evento.nombre}
                      className="absolute inset-0 w-full h-full object-cover"
                    />
                  </div>

                  <CardHeader>
                    <div className="flex justify-between items-start gap-2">
                      <CardTitle className="text-lg line-clamp-2">
                        {evento.nombre}
                      </CardTitle>
                      <Badge
                        variant="outline"
                        className="text-ocre border-ocre shrink-0"
                      >
                        Destacado
                      </Badge>
                    </div>
                  </CardHeader>

                  <CardContent className="flex-1">
                    <p className="text-sm text-muted-foreground line-clamp-3">
                      {evento.descripcion}
                    </p>
                  </CardContent>

                  <CardFooter className="flex justify-between items-center mt-auto border-t pt-4">
                    <span className="text-sm font-semibold">
                      {new Date(evento.fecha).toLocaleDateString()}
                    </span>
                    <Button
                      variant={"link"}
                      asChild
                      className="text-primary text-sm font-bold hover:underline"
                    >
                      <Link to={`/events/${evento.id}`}> Ver más</Link>
                    </Button>
                  </CardFooter>
                </Card>
              </div>
            </CarouselItem>
          ))}
        </CarouselContent>
        <div className="hidden md:block">
          <CarouselPrevious />
          <CarouselNext />
        </div>
      </Carousel>
    </section>
  );
};

export default CarruselEventos;
