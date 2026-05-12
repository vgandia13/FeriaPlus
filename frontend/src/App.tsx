import ProtectedRoute from "./components/ProtectedRoute";
import { BrowserRouter, Route, Routes } from "react-router-dom";
import MainLayout from "./layouts/MainLayout";
import HomePage from "./pages/HomePage";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import EventsPage from "./pages/EventsPage";
import EventPage from "./pages/EventPage";
import NotFoundPage from "./pages/NotFoundPage";
import AdminPage from "./pages/AdminPage";
import { Rol } from "./types/Rol";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<MainLayout />}>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/" element={<HomePage />} />
          <Route path="/events" element={<EventsPage />} />

          <Route element={<ProtectedRoute />}>
            <Route path="/events/:id" element={<EventPage />} />
          </Route>

          {/*TODO: rutas para organizadores y admin */}
          {/* Ruta de admin */}
          <Route element={<ProtectedRoute allowedRoles={[Rol.ROLE_ADMIN]} />}>
            <Route path="/admin" element={<AdminPage />} />
          </Route>

          <Route path="*" element={<NotFoundPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
