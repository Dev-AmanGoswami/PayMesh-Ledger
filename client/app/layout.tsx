import type { Metadata } from "next";
import "./globals.css";

export const metadata: Metadata = {
  title: "Paymesh Pay",
  description: "A Google Pay–style client for the Paymesh Ledger",
};

export default function RootLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <html lang="en">
      <body className="font-sans min-h-screen bg-white">{children}</body>
    </html>
  );
}
