import api from './apiService';
import type { EventoDTO } from '../types/EventoDTO'; // Tu interfaz TypeScript

export const EventoService = {
  // Público según tu SecurityConfig
  getAll: async (params?: {
    nombre?: string;
    categoriaId?: number;
    lat?: number;
    lng?: number;
    radioKm?: number;
    page?: number;
    size?: number;
  }): Promise<{
    content: EventoDTO[];
    page: {
      totalPages: number;
      totalElements: number;
      size: number;
      number: number;
    };
  }> => {
    const response = await api.get('/eventos', { params });
    return response.data;
  },

  obtenerPorId: async (id: number): Promise<EventoDTO> => {
    const response = await api.get(`/eventos/${id}`);
    return response.data;
  },

  // Requiere Rol ORGANIZADOR según tu SecurityConfig
  create: async (evento: EventoDTO): Promise<EventoDTO> => {
    const response = await api.post<EventoDTO>('/eventos', evento);
    return response.data;
  },
};
