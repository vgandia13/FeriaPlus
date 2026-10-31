import { userService } from "@/services/userService";
import { useEffect, useState } from "react";
import { DashboardDTO } from "@/types/DashboardDTO";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogClose,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog";
import ProfileEditForm from "./ProfileEditForm";
import { toast } from "sonner";
import { formatDate } from "@/lib/fechas";

const DashboardPage = () => {
  const [dashboardData, setDashboardData] = useState<DashboardDTO>();
  const [editOpen, setEditOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadDashboard = async () => {
      try {
        setDashboardData(await userService.getDashboardData());
      } catch {
        toast.error("Intente de nuevo.");
        setError("No se pudo cargar el dashboard.");
      } finally {
        setLoading(false);
      }
    };
    loadDashboard();
  }, []);

  if (loading)
    return <div className="text-center p-10">Cargando dashboard...</div>;
  if (error || !dashboardData)
    return <div className="text-center p-10">{error ?? "Sin datos."}</div>;

  return (
    <div className="container mx-auto py-8">
      <Card className="shadow-lg">
        <CardHeader className="bg-primary text-primary-foreground rounded-t-lg">
          <h1 className="text-3xl font-bold text-center">
            Panel de {dashboardData.usuario.nombre}
          </h1>
        </CardHeader>
        <CardContent className="p-6 space-y-8">
          <section>
            <div className="flex items-center justify-between mb-3 border-b pb-2">
              <h2 className="text-xl font-semibold">Información de usuario</h2>
              <Dialog open={editOpen} onOpenChange={setEditOpen}>
                <DialogTrigger asChild>
                  <Button variant="secondary">Editar perfil</Button>
                </DialogTrigger>
                <DialogContent>
                  <DialogHeader>
                    <DialogTitle>Editar perfil</DialogTitle>
                    <DialogDescription>
                      Para cambiar la contraseña tienes que indicar la actual.
                    </DialogDescription>
                  </DialogHeader>
                  <ProfileEditForm
                    user={dashboardData.usuario}
                    onSuccess={async () => {
                      setEditOpen(false);
                      setDashboardData(await userService.getDashboardData());
                    }}
                    BotonCancelar={
                      <DialogClose asChild>
                        <Button type="button" variant="outline">
                          Cancelar
                        </Button>
                      </DialogClose>
                    }
                  />
                </DialogContent>
              </Dialog>
            </div>
            <div className="grid md:grid-cols-2 gap-4">
              <p>
                <span className="font-semibold">Nombre:</span>{" "}
                {dashboardData.usuario.nombre}
              </p>
              <p>
                <span className="font-semibold">Email:</span>{" "}
                {dashboardData.usuario.email}
              </p>
              <p>
                <span className="font-semibold">Rol:</span>{" "}
                {dashboardData.usuario.rol.slice(5).toLocaleLowerCase()}
              </p>
              <p>
                <span className="font-semibold">Fecha de registro:</span>{" "}
                {formatDate(dashboardData.usuario.fechaRegistro)}
              </p>
            </div>
          </section>

          <section>
            <h2 className="text-xl font-semibold mb-3 border-b pb-2">
              Reseñas
            </h2>
            {dashboardData.resenas && dashboardData.resenas.length > 0 ? (
              <div className="grid gap-3">
                {dashboardData.resenas.map((resena) => (
                  <div key={resena.id} className="p-4 bg-secondary rounded-md">
                    <p className="font-bold">
                      Valoración: {resena.valoracion}/5
                    </p>
                    <p className="text-sm">{resena.comentario}</p>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-muted-foreground italic">
                No hay reseñas disponibles
              </p>
            )}
          </section>

          <section>
            <h2 className="text-xl font-semibold mb-3 border-b pb-2">
              Reservas
            </h2>
            {dashboardData.reservas && dashboardData.reservas.length > 0 ? (
              <div className="grid gap-3">
                {dashboardData.reservas.map((reserva) => (
                  <div key={reserva.id} className="p-4 bg-secondary rounded-md">
                    <p>Fecha: {formatDate(reserva.fechaReserva)}</p>
                    <p>
                      Estado:{" "}
                      <span className="font-semibold">{reserva.estado}</span>
                    </p>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-muted-foreground italic">
                No hay reservas realizadas
              </p>
            )}
          </section>

          <section>
            <h2 className="text-xl font-semibold mb-3 border-b pb-2">
              Eventos asistidos
            </h2>
            {dashboardData.eventosAsistidos &&
            dashboardData.eventosAsistidos.length > 0 ? (
              <div className="grid gap-3">
                {dashboardData.eventosAsistidos.map((evento) => (
                  <div key={evento.id} className="p-4 bg-secondary rounded-md">
                    <h3 className="font-bold">{evento.nombre}</h3>
                    <p className="text-sm text-muted-foreground">
                      {evento.descripcion}
                    </p>
                    <p className="text-sm mt-1">
                      Fecha: {formatDate(evento.fecha)}
                    </p>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-muted-foreground italic">
                No has asistido a ningún evento
              </p>
            )}
          </section>

          <section>
            <h2 className="text-xl font-semibold mb-3 border-b pb-2">
              Puestos
            </h2>
            {dashboardData.puestos && dashboardData.puestos.length > 0 ? (
              <div className="grid gap-3">
                {dashboardData.puestos.map((puesto) => (
                  <div key={puesto.id} className="p-4 bg-secondary rounded-md">
                    <p className="font-bold">{puesto.nombre}</p>
                    <p className="text-sm">{puesto.dimensiones}</p>
                    <p className="text-sm font-semibold mt-1">
                      Precio: {puesto.precio}€
                    </p>
                  </div>
                ))}
              </div>
            ) : (
              <p className="text-muted-foreground italic">
                No hay puestos asignados
              </p>
            )}
          </section>
        </CardContent>
      </Card>
    </div>
  );
};

export default DashboardPage;
