package com.popularmovies.vpaliy.popularmoviesapp.ui.fragment;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.popularmovies.vpaliy.domain.model.MovieCover;
import com.popularmovies.vpaliy.domain.model.MovieDetails;
import com.popularmovies.vpaliy.popularmoviesapp.R;
import com.popularmovies.vpaliy.popularmoviesapp.App;
import com.popularmovies.vpaliy.popularmoviesapp.di.module.PresenterModule;
import com.popularmovies.vpaliy.popularmoviesapp.mvp.contract.DetailsMovieContract;
import com.popularmovies.vpaliy.popularmoviesapp.mvp.contract.DetailsMovieContract.Presenter;
import com.popularmovies.vpaliy.popularmoviesapp.ui.adapter.MovieDetailsAdapter;
import com.popularmovies.vpaliy.popularmoviesapp.ui.utils.Constants;
import com.popularmovies.vpaliy.popularmoviesapp.ui.utils.Permission;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class MovieDetailsFragment extends Fragment
    implements DetailsMovieContract.View{

    private Presenter presenter;
    private Unbinder unbinder;

    private MovieDetailsAdapter adapter;

    @BindView(R.id.movieImage)
    protected ImageView movieImage;

    @BindView(R.id.movieDetailsPager)
    protected ViewPager pager;

    @BindView(R.id.tabLayout)
    protected TabLayout tabLayout;

    private int ID;

    public static MovieDetailsFragment newInstance(@NonNull Bundle extraData){
        MovieDetailsFragment fragment=new MovieDetailsFragment();
        fragment.setArguments(extraData);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if(savedInstanceState==null){
            savedInstanceState=getArguments();
        }
        this.ID=savedInstanceState.getInt(Constants.EXTRA_ID);
        initializeDependencies();
    }


    private void initializeDependencies(){
       /* DaggerViewComponent.builder()
                .applicationComponent(App.appInstance().appComponent())
                .presenterModule(new PresenterModule())
                .build().inject(this);  */
    }

    @Override
    public void onStart() {
        super.onStart();
        presenter.start(ID);
    }

    @Override
    public void onStop() {
        super.onStop();
        presenter.stop();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View root=inflater.inflate(R.layout.fragment_movie_details,container,false);
        unbinder=ButterKnife.bind(this,root);
        return root;
    }

    @Override
    public void onViewCreated(View root, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);
        if(root!=null){
            adapter=new MovieDetailsAdapter(getFragmentManager(),ID);
            pager.setAdapter(adapter);
            tabLayout.setupWithViewPager(pager);

        }
    }

    @Inject
    @Override
    public void attachPresenter(@NonNull Presenter presenter) {
        this.presenter=presenter;
        this.presenter.attachView(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @Override
    public void showDetails(@NonNull MovieDetails movie) {

    }

    @Override
    public void showCover(@NonNull MovieCover movieCover) {
        Glide.with(this)
                .fromResource()
                .load(R.drawable.poster)
                .fitCenter()
                .into(new ImageViewTarget<GlideDrawable>(movieImage) {
                    @Override
                    protected void setResource(GlideDrawable resource) {
                        movieImage.setImageDrawable(resource);
                        if(Permission.checkForVersion(Build.VERSION_CODES.LOLLIPOP)){
                            startTransition();
                        }
                    }
                });
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void startTransition(){
        movieImage.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                movieImage.getViewTreeObserver().removeOnPreDrawListener(this);
                getActivity().startPostponedEnterTransition();
                return true;
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(Constants.EXTRA_ID,ID);
        super.onSaveInstanceState(outState);
    }

}