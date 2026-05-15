import CarruselEventos from "@/components/CarruselEventos";

const HomePage = () => {
  return (
    <div className="flex flex-col items-center justify-between gap-16 p-10 w-full max-w-7xl mx-auto">
      <section className="flex flex-col md:flex-row items-center justify-between gap-12 w-full">
        <div className="flex flex-col items-start space-y-6 flex-1">
          <h1 className="text-4xl md:text-6xl font-bold tracking-tight">
            Da vida a tu comunidad con{" "}
            <span className="text-primary">Feria+</span>
          </h1>
          <p className="text-lg text-muted-foreground leading-relaxed max-w-xl">
            ¡Bienvenido a Feria+! Conecta con tu comunidad, comparte tus
            productos y descubre lo que otros tienen para ofrecer. ¡Únete a la
            feria más grande de tu ciudad y haz crecer tu negocio local!
          </p>
        </div>
      </section>

      <section className="w-full pt-4 border-t">
        <CarruselEventos />
      </section>
    </div>
  );
};

export default HomePage;
