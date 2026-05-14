import { userService } from "@/services/userService";
import { useEffect } from "react";

const DashboardPage = () => {
  useEffect(() => {
    const loadUser = async () => {
      const response = await userService.getLoggedUser();
      console.log(response);
    };

    loadUser();
  });

  return (
    <div className="flex flex-col items-center justify-center">
      <h1 className="text-3xl font-bold">Dashboard</h1>
    </div>
  );
};

export default DashboardPage;
