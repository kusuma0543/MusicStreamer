package com.sdsmdg.harjot.MusicDNA;


import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;

import com.sdsmdg.harjot.MusicDNA.CustomBottomSheetDialogs.CustomGeneralBottomSheetDialog;
import com.sdsmdg.harjot.MusicDNA.Helpers.SimpleItemTouchHelperCallback;
import com.sdsmdg.harjot.MusicDNA.Models.LocalTrack;
import com.sdsmdg.harjot.MusicDNA.Models.Track;
import com.sdsmdg.harjot.MusicDNA.Models.UnifiedTrack;
import com.squareup.leakcanary.RefWatcher;

import java.util.Random;


/**
 * A simple {@link Fragment} subclass.
 */
public class RecentsFragment extends Fragment implements RecentsTrackAdapter.OnDragStartListener {

    RecyclerView recentRecycler;
    RecentsTrackAdapter rtAdpater;
    LinearLayoutManager mLayoutManager2;

    LinearLayout noContent;

    ItemTouchHelper mItemTouchHelper;

    onRecentItemClickedListener mCallback;
    onRepeatListener mCallback2;

    FloatingActionButton shuffleFab;

    View bottomMarginLayout;

    public RecentsFragment() {
        // Required empty public constructor
    }

    public interface onRecentItemClickedListener {
        public void onRecentItemClicked(boolean isLocal);

        public void addToPlaylist(UnifiedTrack ut);
    }

    public interface onRepeatListener {
        public void onRecent(int pos);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (onRecentItemClickedListener) context;
            mCallback2 = (onRepeatListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_recents, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bottomMarginLayout = view.findViewById(R.id.bottom_margin_layout);
        if (HomeActivity.isReloaded)
            bottomMarginLayout.getLayoutParams().height = 0;
        else
            bottomMarginLayout.getLayoutParams().height = ((HomeActivity) getContext()).dpTopx(65);

        noContent = (LinearLayout) view.findViewById(R.id.no_recents_content);

        recentRecycler = (RecyclerView) view.findViewById(R.id.view_recent_recycler);
        rtAdpater = new RecentsTrackAdapter(HomeActivity.recentlyPlayed.getRecentlyPlayed(), this, getContext());
        mLayoutManager2 = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recentRecycler.setLayoutManager(mLayoutManager2);
        recentRecycler.setItemAnimator(new DefaultItemAnimator());
        recentRecycler.setAdapter(rtAdpater);

        recentRecycler.addOnItemTouchListener(new ClickItemTouchListener(recentRecycler) {
            @Override
            boolean onClick(RecyclerView parent, View view, int position, long id) {
                UnifiedTrack ut = HomeActivity.recentlyPlayed.getRecentlyPlayed().get(position);
                boolean isRepeat = false;
                int pos = 0;
                for (int i = 0; i < HomeActivity.queue.getQueue().size(); i++) {
                    UnifiedTrack ut1 = HomeActivity.queue.getQueue().get(i);
                    if (ut1.getType() && ut.getType() && ut1.getLocalTrack().getTitle().equals(ut.getLocalTrack().getTitle())) {
                        isRepeat = true;
                        pos = i;
                        break;
                    }
                    if (!ut1.getType() && !ut.getType() && ut1.getStreamTrack().getTitle().equals(ut.getStreamTrack().getTitle())) {
                        isRepeat = true;
                        pos = i;
                        break;
                    }
                }
                if (!isRepeat) {
                    if (ut.getType()) {
                        LocalTrack track = ut.getLocalTrack();
                        if (HomeActivity.queue.getQueue().size() == 0) {
                            HomeActivity.queueCurrentIndex = 0;
                            HomeActivity.queue.getQueue().add(new UnifiedTrack(true, track, null));
                        } else if (HomeActivity.queueCurrentIndex == HomeActivity.queue.getQueue().size() - 1) {
                            HomeActivity.queueCurrentIndex++;
                            HomeActivity.queue.getQueue().add(new UnifiedTrack(true, track, null));
                        } else if (HomeActivity.isReloaded) {
                            HomeActivity.isReloaded = false;
                            HomeActivity.queueCurrentIndex = HomeActivity.queue.getQueue().size();
                            HomeActivity.queue.getQueue().add(new UnifiedTrack(true, track, null));
                        } else {
                            HomeActivity.queue.getQueue().add(++HomeActivity.queueCurrentIndex, new UnifiedTrack(true, track, null));
                        }
                        HomeActivity.localSelectedTrack = track;
                        HomeActivity.streamSelected = false;
                        HomeActivity.localSelected = true;
                        HomeActivity.queueCall = false;
                        HomeActivity.isReloaded = false;
                        mCallback.onRecentItemClicked(true);
                    } else {
                        Track track = ut.getStreamTrack();
                        if (HomeActivity.queue.getQueue().size() == 0) {
                            HomeActivity.queueCurrentIndex = 0;
                            HomeActivity.queue.getQueue().add(new UnifiedTrack(false, null, track));
                        } else if (HomeActivity.queueCurrentIndex == HomeActivity.queue.getQueue().size() - 1) {
                            HomeActivity.queueCurrentIndex++;
                            HomeActivity.queue.getQueue().add(new UnifiedTrack(false, null, track));
                        } else if (HomeActivity.isReloaded) {
                            HomeActivity.isReloaded = false;
                            HomeActivity.queueCurrentIndex = HomeActivity.queue.getQueue().size();
                            HomeActivity.queue.getQueue().add(new UnifiedTrack(false, null, track));
                        } else {
                            HomeActivity.queue.getQueue().add(++HomeActivity.queueCurrentIndex, new UnifiedTrack(false, null, track));
                        }
                        HomeActivity.selectedTrack = track;
                        HomeActivity.streamSelected = true;
                        HomeActivity.localSelected = false;
                        HomeActivity.queueCall = false;
                        HomeActivity.isReloaded = false;
                        mCallback.onRecentItemClicked(false);
                    }
                } else {
                    mCallback2.onRecent(pos);
                }

                return true;
            }

            @Override
            boolean onLongClick(RecyclerView parent, View view, int position, long id) {
                final UnifiedTrack ut = HomeActivity.recentlyPlayed.getRecentlyPlayed().get(position);
                CustomGeneralBottomSheetDialog generalBottomSheetDialog = new CustomGeneralBottomSheetDialog();
                generalBottomSheetDialog.setPosition(position);
                generalBottomSheetDialog.setTrack(ut);
                generalBottomSheetDialog.setFragment("Recents");
                generalBottomSheetDialog.show(getActivity().getSupportFragmentManager(), "general_bottom_sheet_dialog");
                return true;
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

            }
        });

        shuffleFab = (FloatingActionButton) view.findViewById(R.id.play_all_fab_recent);

        if (HomeActivity.recentlyPlayed != null && HomeActivity.recentlyPlayed.getRecentlyPlayed().size() > 0) {
            noContent.setVisibility(View.INVISIBLE);
            shuffleFab.setVisibility(View.VISIBLE);
        } else {
            noContent.setVisibility(View.VISIBLE);
            shuffleFab.setVisibility(View.INVISIBLE);
        }

        shuffleFab.setBackgroundTintList(ColorStateList.valueOf(HomeActivity.themeColor));
        shuffleFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (HomeActivity.recentlyPlayed.getRecentlyPlayed().size() > 0) {
                    HomeActivity.queue.getQueue().clear();
                    for (int i = 0; i < HomeActivity.recentlyPlayed.getRecentlyPlayed().size(); i++) {
                        HomeActivity.queue.getQueue().add(HomeActivity.recentlyPlayed.getRecentlyPlayed().get(i));
                    }
                    Random r = new Random();
                    mCallback2.onRecent(r.nextInt(HomeActivity.queue.getQueue().size()));
                }
            }
        });

        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(rtAdpater);
        mItemTouchHelper = new ItemTouchHelper(callback);
        mItemTouchHelper.attachToRecyclerView(recentRecycler);

    }

    @Override
    public void onDragStarted(RecyclerView.ViewHolder viewHolder) {

    }

    @Override
    public void onResume() {
        super.onResume();
        mLayoutManager2.scrollToPositionWithOffset(0, 0);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                shuffleFab.animate().scaleX(1.0f).scaleY(1.0f).setDuration(300).setInterpolator(new OvershootInterpolator());
            }
        }, 500);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        RefWatcher refWatcher = MusicDNAApplication.getRefWatcher(getContext());
        refWatcher.watch(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = MusicDNAApplication.getRefWatcher(getContext());
        refWatcher.watch(this);
    }
}
