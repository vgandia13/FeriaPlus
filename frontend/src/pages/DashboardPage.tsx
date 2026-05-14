import { userService } from "@/services/userService";
import { useEffect } from "react";

const DashboardPage = () => {
  useEffect(() => {
    const loadDashboard = async () => {
      const response = await userService.getDashboardData();
      console.log(response);
    };

    loadDashboard();
  });

  return (
    <div className="flex flex-col items-center justify-center">
      <h1 className="text-3xl font-bold">Dashboard</h1>
    </div>
  );
};

export default DashboardPage;
