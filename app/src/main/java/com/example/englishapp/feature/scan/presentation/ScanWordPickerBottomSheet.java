package com.example.englishapp.feature.scan.presentation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.englishapp.R;
import com.example.englishapp.feature.scan.presentation.ScanWordAdapter;
import com.example.englishapp.feature.scan.domain.model.WordCandidate;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class ScanWordPickerBottomSheet extends BottomSheetDialogFragment {

    public interface OnWordSelectedListener{
        void onWordSelected(String word);
    }

    private RecyclerView recyclerView;
    private SearchView searchView;

    private ScanWordAdapter adapter;

    private final List<WordCandidate> originalList = new ArrayList<>();

    private OnWordSelectedListener listener;

    public void setOnWordSelectedListener(OnWordSelectedListener listener){
        this.listener = listener;
    }

    public void submitWords(List<WordCandidate> words){

        originalList.clear();

        originalList.addAll(words);

        if(adapter!=null){
            adapter.submitList(new ArrayList<>(originalList));
        }

    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {

        return inflater.inflate(
                R.layout.bottom_sheet_scan_words,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view,savedInstanceState);

        recyclerView=view.findViewById(R.id.recyclerWords);
        searchView=view.findViewById(R.id.searchWord);

        recyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext())
        );

        adapter=new ScanWordAdapter(candidate -> {

            dismiss();

            if(listener!=null){

                listener.onWordSelected(candidate.getWord());

            }

        });

        recyclerView.setAdapter(adapter);

        adapter.submitList(new ArrayList<>(originalList));

        searchView.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {

                    @Override
                    public boolean onQueryTextSubmit(String query) {

                        filter(query);

                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {

                        filter(newText);

                        return true;
                    }
                });

    }

    private void filter(String keyword){

        if(keyword==null){

            adapter.submitList(new ArrayList<>(originalList));

            return;

        }

        keyword=keyword.trim().toLowerCase();

        if(keyword.isEmpty()){

            adapter.submitList(new ArrayList<>(originalList));

            return;

        }

        List<WordCandidate> result=new ArrayList<>();

        for(WordCandidate item:originalList){

            if(item.getWord().toLowerCase().contains(keyword)){

                result.add(item);

            }

        }

        adapter.submitList(result);

    }

}