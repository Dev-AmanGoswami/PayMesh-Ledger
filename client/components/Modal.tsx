"use client";

import { ReactNode } from "react";

export default function Modal({
  open,
  title,
  onClose,
  children,
}: {
  open: boolean;
  title: string;
  onClose: () => void;
  children: ReactNode;
}) {
  if (!open) return null;
  return (
    <div
      className="fixed inset-0 z-50 flex items-end sm:items-center justify-center bg-black/40 p-0 sm:p-4"
      onClick={onClose}
    >
      <div
        className="w-full sm:max-w-md bg-white rounded-t-3xl sm:rounded-3xl shadow-gcard p-6 animate-[slideUp_.2s_ease-out]"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-center justify-between mb-5">
          <h2 className="text-xl font-medium text-gtext">{title}</h2>
          <button
            onClick={onClose}
            className="h-9 w-9 rounded-full hover:bg-gsurface text-gmuted text-2xl leading-none flex items-center justify-center"
            aria-label="Close"
          >
            ×
          </button>
        </div>
        {children}
      </div>
      <style>{`@keyframes slideUp{from{transform:translateY(16px);opacity:.6}to{transform:translateY(0);opacity:1}}`}</style>
    </div>
  );
}
