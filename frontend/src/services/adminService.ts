import api from './apiService';
import { UsuarioResponseDTO } from '@/types/UsuarioResponseDTO';

export const adminService = {
  getAllUsers: async (params?: { nombre?: string; page?: number; size?: number }): Promise<{ content: UsuarioResponseDTO[]; totalPages: number; totalElements: number }> => {
    const response = await api.get('/admin/usuarios', { params });
    return response.data;
  },

  deleteUser: async (id: string): Promise<void> => {
    await api.delete(`/admin/usuarios/${id}`);
  },

  getUser: async (id: string): Promise<UsuarioResponseDTO> => {
    const response = await api.get(`/admin/usuarios/${id}`);
    return response.data;
  },

  updateUser: async (user: Partial<UsuarioResponseDTO>): Promise<Partial<UsuarioResponseDTO>> => {
    const response = await api.put<UsuarioResponseDTO>(`/admin/usuarios/${user.id}`, user);
    return response.data;
  },
};
