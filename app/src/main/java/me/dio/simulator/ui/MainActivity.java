package me.dio.simulator.ui;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.snackbar.Snackbar;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import me.dio.simulator.R;
import me.dio.simulator.data.MatchesAPI;
import me.dio.simulator.databinding.ActivityMainBinding;
import me.dio.simulator.domain.Match;
import me.dio.simulator.ui.adapter.MatchesAdapter;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity<i> extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MatchesAPI matches_api;
    private MatchesAdapter matchesAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupHttpClient();
        setupMatchesList();
        setupMatchesRefresh();
        FloatingActiongButton();
    }


    private void setupHttpClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://nikireis.github.io/Matches-Simulator-API/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        matches_api = retrofit.create(MatchesAPI.class);
    }

    private void setupMatchesList(){
        binding.rvMatches.setHasFixedSize(true);
        binding.rvMatches.setLayoutManager(new LinearLayoutManager(this));
        matchesAdapter = new MatchesAdapter(Collections.emptyList());
        binding.rvMatches.setAdapter(matchesAdapter);
        findMatchesFromApi();
    }



    private void setupMatchesRefresh(){
        binding.srlMatches.setOnRefreshListener(this::findMatchesFromApi);

    }

    private void FloatingActiongButton() {
        binding.fabSimulate.setOnClickListener(view -> {
            view.animate().rotationBy(360).setDuration(500).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    Random random = new Random();
                    for (int i = 0; i < matchesAdapter.getItemCount(); i++) {
                        Match match = matchesAdapter.getMatches().get(i);
                        match.getHomeTeam().setScore(random.nextInt(match.getHomeTeam().getStars() + 1));
                        match.getAwayTeam().setScore(random.nextInt(match.getAwayTeam().getStars() + 1));
                        matchesAdapter.notifyItemChanged(i);
                    }
                }
            });
        });
    }

    private void findMatchesFromApi() {
        binding.srlMatches.setRefreshing(true);
        matches_api.getMatches().enqueue(new Callback<List<Match>>() {
            @Override
            public void onResponse(Call<List<Match>> call, Response<List<Match>> response) {
                if(response.isSuccessful()){
                    List<Match> matches = response.body();
                    matchesAdapter = new MatchesAdapter(matches);
                    binding.rvMatches.setAdapter(matchesAdapter);
                    binding.srlMatches.setRefreshing(false);

                }else{
                    showMessageError();
                }
                binding.srlMatches.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<List<Match>> call, Throwable t) {
                showMessageError();
                binding.srlMatches.setRefreshing(false);
            }
        });
    }

    private void showMessageError(){
        Snackbar.make(binding.fabSimulate, R.string.error_api, Snackbar.LENGTH_LONG).show();
    }

}

