import React, { useState } from "react";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import {
  Bar,
  BarChart,
  CartesianGrid,
  XAxis,
  Tooltip as RechartsTooltip,
  Pie,
  PieChart,
  Label,
} from "recharts";

interface Invoice {
  sellerName: string;
  invoiceReference: string;
  invoiceTypeCode: number;
  issuedDate: string;
  totalSum: number;
  fileFormat: string;
}

interface GraphsPanelProps {
  invoices: Invoice[];
}

export const GraphsPanel: React.FC<GraphsPanelProps> = ({ invoices }) => {
  // Get unique years from the data
  const years = Array.from(
    new Set(invoices.map((invoice) => new Date(invoice.issuedDate).getFullYear()))
  ).sort((a, b) => b - a);

  const currentYear = new Date().getFullYear();
  const [selectedYear, setSelectedYear] = useState(currentYear);

  // Filter invoices by the selected year
  const filteredInvoices = invoices.filter(
    (invoice) => new Date(invoice.issuedDate).getFullYear() === selectedYear
  );

  // Process data for graphs
  const sumPerMonth = filteredInvoices.reduce((acc, invoice) => {
    const month = new Date(invoice.issuedDate).toLocaleString("default", { month: "long" });
    acc[month] = (acc[month] || 0) + invoice.totalSum;
    return acc;
  }, {} as Record<string, number>);

  const topSellers = filteredInvoices.reduce((acc, invoice) => {
    acc[invoice.sellerName] = (acc[invoice.sellerName] || 0) + 1;
    return acc;
  }, {} as Record<string, number>);

  const fileFormatDistribution = filteredInvoices.reduce((acc, invoice) => {
    acc[invoice.fileFormat] = (acc[invoice.fileFormat] || 0) + 1;
    return acc;
  }, {} as Record<string, number>);

  // Transform data for charts
  const barData = Object.entries(sumPerMonth).map(([month, sum]) => ({ month, amount: sum }));
  const sellerData = Object.entries(topSellers)
    .sort(([, a], [, b]) => b - a)
    .slice(0, 5)
    .map(([seller, count], index) => ({
      seller,
      count,
      fill: `hsl(var(--chart-${(index % 5) + 1}))`,
    }));

  const pieData = Object.entries(fileFormatDistribution).map(([format, count], index) => ({
    format,
    count,
    fill: `hsl(var(--chart-${(index % 5) + 1}))`,
  }));

  return (
    <div className="mb-8">
      {/* Year Selection */}
      <div className="mb-4 flex items-center justify-between">
        <h2 className="text-lg font-bold">Graphs for {selectedYear}</h2>
        <Select
          defaultValue={currentYear.toString()}
          onValueChange={(value) => setSelectedYear(parseInt(value))}
        >
          <SelectTrigger className="w-[200px]">
            <SelectValue placeholder="Select Year" />
          </SelectTrigger>
          <SelectContent>
            {years.map((year) => (
              <SelectItem key={year} value={year.toString()}>
                {year}
              </SelectItem>
            ))}
          </SelectContent>
        </Select>
      </div>

      {/* Graphs */}
      <div className="grid grid-cols-3 gap-4">
        {/* Bar Chart: Sum per Month */}
        <Card>
          <CardHeader>
            <CardTitle>Sum per Month</CardTitle>
            <CardDescription>Monthly total invoice sums</CardDescription>
          </CardHeader>
          <CardContent>
            <BarChart width={300} height={200} data={barData}>
              <CartesianGrid vertical={false} stroke="hsl(var(--border))" />
              <XAxis
                dataKey="month"
                tickLine={false}
                tickMargin={10}
                axisLine={false}
                tick={{ fill: "hsl(var(--foreground))" }}
              />
              <RechartsTooltip
                formatter={(value: number) =>
                  `${value.toLocaleString("en-US", { style: "currency", currency: "USD" })}`
                }
                labelFormatter={(label: string) => `Month: ${label}`}
                contentStyle={{
                  backgroundColor: "hsl(var(--card))",
                  border: "1px solid hsl(var(--border))",
                }}
                itemStyle={{ color: "hsl(var(--foreground))" }}
                labelStyle={{ color: "hsl(var(--muted-foreground))" }}
              />
              <Bar dataKey="amount" fill="hsl(var(--chart-1))" radius={8} />
            </BarChart>
          </CardContent>
        </Card>

        {/* Bar Chart: Top Sellers */}
        <Card>
          <CardHeader>
            <CardTitle>Top 5 Sellers</CardTitle>
            <CardDescription>Most associated invoices</CardDescription>
          </CardHeader>
          <CardContent>
            <BarChart width={300} height={200} data={sellerData}>
              <CartesianGrid vertical={false} stroke="hsl(var(--border))" />
              <XAxis
                dataKey="seller"
                tickLine={false}
                tickMargin={10}
                axisLine={false}
                tick={{ fill: "hsl(var(--foreground))" }}
              />
              <RechartsTooltip
                formatter={(value: number) => `${value} Invoices`}
                labelFormatter={(label: string) => `Seller: ${label}`}
                contentStyle={{
                  backgroundColor: "hsl(var(--card))",
                  border: "1px solid hsl(var(--border))",
                }}
                itemStyle={{ color: "hsl(var(--foreground))" }}
                labelStyle={{ color: "hsl(var(--muted-foreground))" }}
              />
              <Bar dataKey="count" fill="hsl(var(--chart-2))" radius={8} />
            </BarChart>
          </CardContent>
        </Card>

        {/* Pie Chart: File Format Distribution */}
        <Card>
          <CardHeader>
            <CardTitle>File Format Distribution</CardTitle>
            <CardDescription>Types of invoice files</CardDescription>
          </CardHeader>
          <CardContent>
            <PieChart width={250} height={250}>
              <RechartsTooltip
                formatter={(value: number, name: string) => `${value} (${name})`}
                contentStyle={{
                  backgroundColor: "hsl(var(--card))",
                  border: "1px solid hsl(var(--border))",
                }}
                itemStyle={{ color: "hsl(var(--foreground))" }}
                labelStyle={{ color: "hsl(var(--muted-foreground))" }}
              />
              <Pie data={pieData} dataKey="count" nameKey="format" outerRadius={80}>
                <Label position="center">
                  {pieData.reduce((acc, item) => acc + item.count, 0)} Invoices
                </Label>
              </Pie>
            </PieChart>
          </CardContent>
        </Card>
      </div>
    </div>
  );
};
