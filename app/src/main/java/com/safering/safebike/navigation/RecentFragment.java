package com.safering.safebike.navigation;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.safering.safebike.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecentFragment extends Fragment {
    private static final String KEY_POI_OBJECT = "poiobject";
//    private static final String KEY_POI_NAME = "poiName";
//    private static final String KEY_POI_LATITUDE = "poiLatitude";
//    private static final String KEY_POI_LONGITUDE = "poiLongitude";
//    private static final String KEY_POI_ADDRESS = "poiAddress";
    private static final String TAG_TAB_RECENT = "RECENT";

    FragmentTabHost tabHost;

    ListView listView;
    SimpleCursorAdapter mAdapter;
    TextView messageView;
    Button deleteRctBtn;

    int nameColumnIndex = -1;

    public RecentFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Toast.makeText(getContext(), "RecentFragment.onCreate", Toast.LENGTH_SHORT).show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
//        Toast.makeText(getContext(), "RecentFragment.onCreateView", Toast.LENGTH_SHORT).show();
        View view = inflater.inflate(R.layout.fragment_recent, container, false);

        messageView = (TextView) view.findViewById(R.id.text_messag_recent);
        listView = (ListView) view.findViewById(R.id.listView_recent);

        String[] from = {RecentDB.RecentTable.COLUMN_POI_NAME, RecentDB.RecentTable.COLUMN_SEARCH_DATE};
        int[] to = {R.id.text_rct_poi_name, R.id.text_rct_search_date};

        mAdapter = new SimpleCursorAdapter(getContext(), R.layout.recent_item_view, null, from, to, SimpleCursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (columnIndex == nameColumnIndex) {
                    /*
                     *  View 에 글 넣어주기
                     */
                    TextView tvRctPoiName = (TextView) view.findViewById(R.id.text_rct_poi_name);
                    tvRctPoiName.setText(cursor.getString(columnIndex));

                    return true;
                }

                return false;
            }
        });

        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                Toast.makeText(getContext(), "clicked", Toast.LENGTH_SHORT).show();
                Cursor c = (Cursor) listView.getItemAtPosition(position);
                final String rctPoiName = c.getString(c.getColumnIndex(RecentDB.RecentTable.COLUMN_POI_NAME));
                /*
                 * ParentRctFvActivity 에 있는 setResult 처리
                 */

                Log.d("safebike", "rctPoiName : " + rctPoiName);

                NavigationNetworkManager.getInstance().searchPOI(getContext(), rctPoiName, new NavigationNetworkManager.OnResultListener<SearchPOIInfo>() {
                    @Override
                    public void onSuccess(SearchPOIInfo result) {
                        POI poi = result.pois.poiList.get(0);

                        if (poi != null) {
                            Log.d("safebike", "poi.secondNo : " + poi.secondNo);

                            if (getActivity() != null) {
                                ((ParentRctFvActivity) getActivity()).sendPOI(poi);

                                Log.d("safebike", "RecentFragment.onCreateView.onSuccess.sendPOI.getActivity != null | ((ParentRctFvActivity) getActivity()).sendPOI(poi)");

                            }
//                            sendPOI(poi);
                        }
                    }

                    @Override
                    public void onFail(int code) {

                    }
                });
            }
        });

        deleteRctBtn = (Button) view.findViewById(R.id.btn_delete_recent);
        deleteRctBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setIcon(android.R.drawable.ic_dialog_info);
//        builder.setTitle("전체 항목 삭제");
                builder.setMessage("전체 항목을 삭제하시겠습니까");
                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RecentDataManager.getInstance().deleteRecentAll();

                        Cursor c = RecentDataManager.getInstance().getRecentCursor(null);
                        mAdapter.changeCursor(c);
                        mAdapter.notifyDataSetChanged();
                        mAdapter.notifyDataSetInvalidated();

                        messageView.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
                        deleteRctBtn.setVisibility(View.GONE);

                    }
                });
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
//        builder.setCancelable(false);

                builder.create().show();
            }
        });

        /*
         *  DB 있을 떄 없을 때 뷰 처리
         */

        Cursor c = RecentDataManager.getInstance().getRecentCursor(null);

        if (c.getCount() > 0) {
            messageView.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
            deleteRctBtn.setVisibility(View.VISIBLE);
        } else {

        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
//        Toast.makeText(getContext(), "RecentFragment.onResume", Toast.LENGTH_SHORT).show();
        Cursor c = RecentDataManager.getInstance().getRecentCursor(null);
        nameColumnIndex = c.getColumnIndex(RecentDB.RecentTable.COLUMN_POI_NAME);

        mAdapter.changeCursor(c);
        mAdapter.notifyDataSetChanged();
        mAdapter.notifyDataSetInvalidated();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
//        Toast.makeText(getContext(), "RecentFragment.onDestroy", Toast.LENGTH_SHORT).show();
        mAdapter.changeCursor(null);
    }

    private void sendPOI(POI poi) {
//        String defineAddress = null;
//
//        if (!poi.detailAddrName.equals("") && !poi.firstNo.equals("") && !poi.secondNo.equals("")) {
//            defineAddress = poi.getAddress() + " "+ poi.getDetailAddress();
//
//            Log.d("safebike", "defineAddress 1");
//        } else if (!poi.detailAddrName.equals("") && !poi.firstNo.equals("") && poi.secondNo.equals("")) {
//            defineAddress = poi.getAddress() + " " + poi.firstNo;
//
//            Log.d("safebike", "defineAddress 2");
//        } else if (!poi.detailAddrName.equals("") && poi.firstNo.equals("") && poi.secondNo.equals("")) {
//            defineAddress = poi.getAddress();
//
//            Log.d("safebike", "defineAddress 3");
//        } else if (poi.detailAddrName.equals("") && !poi.firstNo.equals("") && !poi.secondNo.equals("")) {
//            defineAddress = poi.middleAddrName + " " + poi.lowerAddrName + " " + poi.getDetailAddress();
//
//            Log.d("safebike", "defineAddress 4");
//        } else if (poi.detailAddrName.equals("") && !poi.firstNo.equals("") && poi.secondNo.equals("")) {
//            defineAddress = poi.getAddress() + " " + poi.firstNo;
//
//            Log.d("safebike", "defineAddress 5");
//        } else if (poi.detailAddrName.equals("") && poi.firstNo.equals("") && poi.secondNo.equals("")) {
//            defineAddress = poi.middleAddrName + " " + poi.lowerAddrName;
//
//            Log.d("safebike", "defineAddress 6");
//        } else {
//            defineAddress = poi.getAddress() + " " + poi.getDetailAddress();
//
//            Log.d("safebike", "defineAddress 7");
//        }


//        if (getActivity() != null) {
//            Log.d("safebike", "RecentFragment.onCreateView.onSuccess.sendPOI.recentFragment != null");
//
//            Intent intent = new Intent();
//            intent.putExtra(KEY_POI_OBJECT, poi);
//            getActivity().setResult(Activity.RESULT_OK, intent);
//
//            getActivity().finish();
//        }
    }

}
