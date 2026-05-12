import api from './apiService';
import { UsuarioResponseDTO } from '@/types/UsuarioResponseDTO';

export const adminService = {
  getAllUsers: async (): Promise<UsuarioResponseDTO[]> => {
    const response = await api.get('/admin/usuarios');
    return response.data;
  },

  deleteUser: async (id: string): Promise<void> => {
    await api.delete(`/admin/usuarios/${id}`);
  },
};
