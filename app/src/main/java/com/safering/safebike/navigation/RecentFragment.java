package com.safering.safebike.navigation;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.safering.safebike.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class RecentFragment extends Fragment {
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
        Toast.makeText(getContext(), "RecentFragment.onCreate", Toast.LENGTH_SHORT).show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Toast.makeText(getContext(), "RecentFragment.onCreateView", Toast.LENGTH_SHORT).show();
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

                    return true;
                }

                return false;
            }
        });

        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getContext(), "clicked", Toast.LENGTH_SHORT).show();

                /*
                 * ParentRctFvActivity 에 있는 setResult 처리
                 */
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
                        mAdapter.notifyDataSetChanged();
                        /*
                         *  Adpater clear 처리 필요
                         */

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
        Toast.makeText(getContext(), "RecentFragment.onResume", Toast.LENGTH_SHORT).show();
        Cursor c = RecentDataManager.getInstance().getRecentCursor(null);
        nameColumnIndex = c.getColumnIndex(RecentDB.RecentTable.COLUMN_POI_NAME);

        mAdapter.changeCursor(c);
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(getContext(), "RecentFragment.onDestroy", Toast.LENGTH_SHORT).show();
        mAdapter.changeCursor(null);
    }
}
