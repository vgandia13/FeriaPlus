export interface DashboardDTO {
  usuario: {
    id: string;
    nombre: string;
    email: string;
    rol: string;
    fechaRegistro: string;
  };
  resenas: Array<{
    id: number;
    valoracion: number;
    comentario: string;
    ubicacionId: number;
    eventoId: number;
  }>;
  reservas: Array<{
    id: number;
    fechaReserva: string;
    estado: string;
    expositorId: number;
    puestoId: number;
  }>;
  eventosAsistidos: Array<{
    id: number;
    nombre: string;
    descripcion: string;
    fecha: string;
    imagenUrl: string;
    categoriaId: number;
    organizadorId: number;
  }>;
  puestos: Array<{
    id: number;
    nombre: string;
    descripcion: string;
    precio: number;
  }>;
}
