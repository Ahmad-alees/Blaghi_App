package com.example.balaghi.adapters;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.balaghi.R;
import com.example.balaghi.databinding.ItemReportBinding;
import com.example.balaghi.models.Report;

import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.ReportViewHolder> {

    private final List<Report> reportList;
    private final OnReportClickListener listener;

    public interface OnReportClickListener {
        void onReportClick(Report report);
        void onEditClick(Report report);
        void onDeleteClick(Report report);
    }

    public ReportAdapter(List<Report> reportList, OnReportClickListener listener) {
        this.reportList = reportList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ReportViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemReportBinding binding = ItemReportBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ReportViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ReportViewHolder holder, int position) {
        holder.bind(reportList.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return reportList.size();
    }

    static class ReportViewHolder extends RecyclerView.ViewHolder {

        private final ItemReportBinding binding;

        ReportViewHolder(ItemReportBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Report report, OnReportClickListener listener) {
            binding.tvReportTitle.setText(report.getTitle());
            binding.tvReportCategory.setText(report.getCategory());
            binding.tvReportDate.setText(report.getDate());
            binding.tvReportLocation.setText(report.getLocation());
            binding.chipStatus.setText(report.getStatus());

            switch (report.getStatus()) {
                case "تم الحل":
                    binding.chipStatus.setChipBackgroundColorResource(R.color.status_green);
                    break;
                case "قيد المعالجة":
                    binding.chipStatus.setChipBackgroundColorResource(R.color.status_orange);
                    break;
                case "مرفوض":
                    binding.chipStatus.setChipBackgroundColorResource(R.color.status_red);
                    break;
                default:
                    binding.chipStatus.setChipBackgroundColorResource(R.color.status_blue);
                    break;
            }

            binding.getRoot().setOnClickListener(v -> listener.onReportClick(report));

            binding.btnEditReport.setOnClickListener(v -> listener.onEditClick(report));

            binding.btnDeleteReport.setOnClickListener(v ->
                    new AlertDialog.Builder(binding.getRoot().getContext())
                            .setTitle("حذف البلاغ")
                            .setMessage("هل أنت متأكد من حذف هذا البلاغ؟")
                            .setPositiveButton("حذف", (dialog, which) -> listener.onDeleteClick(report))
                            .setNegativeButton("إلغاء", null)
                            .show());
        }
    }
}
