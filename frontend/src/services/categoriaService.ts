import api from './apiService';
import type { CategoriaDTO } from '../types/CategoriaDTO';

export const CategoriaService = {
  getAll: async (): Promise<CategoriaDTO[]> => {
    const response = await api.get<CategoriaDTO[]>('/categorias');
    return response.data;
  },
};
