import { Outlet } from "react-router-dom";
import Navbar from "@/components/Navbar";
import { Toaster } from "sonner";
import { Suspense } from "react";

const MainLayout = () => {
  return (
    <div className="min-h-screen bg-background">
      <Navbar />
      <main className="container mx-auto ">
        <Suspense fallback={null}>
          <Outlet />
        </Suspense>
        <Toaster position="top-center" />
      </main>
    </div>
  );
};

export default MainLayout;
