import { useEffect, useState } from "react";
import {
  RadialBarChart,
  RadialBar,
  Legend,
  ResponsiveContainer,
  Tooltip,
} from "recharts";

function App() {
  const [metrics, setMetrics] = useState([]);

  useEffect(() => {
    const fetchData = () => {
      fetch("http://localhost:8082/api/metrics/latest")
        .then((res) => res.json())
        .then((data) => setMetrics(data))
        .catch((err) => console.error(err));
    };

    fetchData();
    const interval = setInterval(fetchData, 3000);
    return () => clearInterval(interval);
  }, []);

  const getLatestPerService = () => {
    const seen = {};
    metrics.forEach((m) => {
      if (!seen[m.serviceName]) seen[m.serviceName] = m;
    });
    return Object.values(seen);
  };

  const serviceColors = {
    "order-service": "#38bdf8",
    "payment-service": "#22c55e",
    "inventory-service": "#f59e0b",
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-950 via-slate-900 to-indigo-950 p-8 text-white">
      <h1 className="text-4xl font-extrabold text-center mb-10 text-cyan-300">
        System Metrics Dashboard
      </h1>

      {/* CPU Chart */}
      <div className="mb-10 rounded-3xl bg-white/10 backdrop-blur-xl border border-white/20 shadow-2xl p-6">
        <h2 className="text-2xl font-bold text-cyan-300 mb-5">
          CPU Usage (Latest)
        </h2>

        <div style={{ width: "100%", height: 350 }}>
          <ResponsiveContainer>
            <RadialBarChart
              innerRadius="20%"
              outerRadius="90%"
              data={getLatestPerService().map((m) => ({
                name: m.serviceName,
                cpu: m.cpuUsage,
                fill:
                  m.cpuUsage > 85
                    ? "#ef4444"
                    : serviceColors[m.serviceName] || "#38bdf8",
              }))}
              startAngle={180}
              endAngle={0}
            >
              <RadialBar minAngle={15} background clockWise dataKey="cpu" />

              <Legend
                iconSize={12}
                layout="vertical"
                verticalAlign="middle"
                align="right"
                wrapperStyle={{
                  color: "#fff",
                  fontSize: "14px",
                }}
              />

              <Tooltip
                contentStyle={{
                  background: "linear-gradient(135deg, #ffffff, #f8fafc)",
                  border: "1px solid #38bdf8",
                  borderRadius: "14px",
                  boxShadow: "0 15px 35px rgba(0,0,0,0.35)",
                  padding: "12px",
                }}
                labelStyle={{
                  color: "#0f172a",
                  fontWeight: "700",
                }}
                itemStyle={{
                  color: "#0284c7",
                  fontWeight: "600",
                  fontSize: "14px",
                }}
              />
            </RadialBarChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Metrics Table */}

      <div className="overflow-hidden rounded-3xl bg-white/10 backdrop-blur-xl border border-white/20 shadow-2xl">
        <table className="w-full border-collapse">
          <thead>
            <tr className="bg-cyan-600 text-white uppercase tracking-widest text-sm">
              <th className="p-5">Service</th>
              <th className="p-5">CPU %</th>
              <th className="p-5">DB Conn</th>
              <th className="p-5">Response</th>
              <th className="p-5">Status</th>
            </tr>
          </thead>

          <tbody>
            {metrics.map((m, index) => {
              const isAnomalous =
                m.cpuUsage > 85 ||
                m.dbConnections > 90 ||
                m.responseTimeMs > 250;

              return (
                <tr
                  key={m.id}
                  className={`transition duration-300 border-b border-white/10 ${
                    isAnomalous
                      ? "bg-red-900/30 hover:bg-red-800/40"
                      : index % 2 === 0
                        ? "bg-white/5 hover:bg-cyan-500/10"
                        : "bg-white/10 hover:bg-cyan-500/10"
                  }`}
                >
                  <td className="p-5 font-semibold text-white">
                    {m.serviceName}
                  </td>

                  <td className="p-5 text-gray-200">
                    {m.cpuUsage.toFixed(2)}%
                  </td>

                  <td className="p-5 text-gray-200">{m.dbConnections}</td>

                  <td className="p-5 text-gray-200">{m.responseTimeMs} ms</td>

                  <td className="p-5">
                    <span
                      className={`px-4 py-2 rounded-full text-xs font-bold ${
                        isAnomalous
                          ? "bg-red-500/20 text-red-300 border border-red-500"
                          : "bg-emerald-500/20 text-emerald-300 border border-emerald-500"
                      }`}
                    >
                      {isAnomalous ? "⚠ Alert" : "✔ Healthy"}
                    </span>
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default App;
