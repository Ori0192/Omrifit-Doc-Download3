//package com.example.omrifit.manager;
//
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.Menu;
//import android.view.MenuInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.view.inputmethod.EditorInfo;
//import android.widget.EditText;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.example.omrifit.R;
//import com.example.omrifit.adapters.MessageAdapter;
//import com.example.omrifit.classes.Message;
//import com.example.omrifit.fragments.HomePageActivity;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//
//import java.util.ArrayList;
//
//public class FragmentManageMessages extends Fragment {
//    RecyclerView recyclerView;
//    EditText message_edt_text;
//    ArrayList<Message> messageList;
//    MessageAdapter messageAdapter;
//    FirebaseAuth mAuth = FirebaseAuth.getInstance();
//    FirebaseUser user = mAuth.getCurrentUser();
//    FirebaseDatabase database = FirebaseDatabase.getInstance();
//    DatabaseReference myRef = database.getReference("omri_chat").child("everyone");
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_chat_fragemant, container, false);
//
//        recyclerView = view.findViewById(R.id.recycler_view_search);
//        message_edt_text = view.findViewById(R.id.edit_text);
//
//        messageList = new ArrayList<>();
//        myRef.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                Message message=new Message();
//                messageList.clear();
//                for (DataSnapshot shot:snapshot.getChildren()){
//                    message=shot.getValue(Message.class);
//                    messageList.add(message);
//                }
//                setuprecyclerview(messageList);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//
//        message_edt_text.setOnEditorActionListener((v, actionId, event) -> {
//            if (actionId == EditorInfo.IME_ACTION_SEND) {
//                addtochat(message_edt_text.getText().toString());
//                return true;
//            }
//            return false;
//        });
//        return view;
//    }
//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        // Inflate your menu resource or modify the menu
//        inflater.inflate(R.menu.main_menu, menu);
//    }
//    public void addtochat(String response){
//        messageList.add(new Message(response,"Omri"));
//        myRef.setValue(messageList);
//    }
//    public void setuprecyclerview(ArrayList<Message> messageList){
////        messageAdapter = new MessageAdapter(requireContext(),messageList,myRef);
////        recyclerView.setAdapter(messageAdapter);
////        LinearLayoutManager llm = new LinearLayoutManager(new HomePageActivity());
////        llm.setStackFromEnd(true);
////        recyclerView.setLayoutManager(llm);
//    }
//}
//
