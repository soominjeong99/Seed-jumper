package gachon.mpclass.seedjumper;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.dinuscxj.progressbar.CircleProgressBar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class fragmentHome extends Fragment implements CircleProgressBar.ProgressFormatter{

    private TextView amount_calorie_consumption;
    private Button normal_btn;
    private Button recommend_btn;
    private double calorie, planCalorie;

    CircleProgressBar circleProgressBar;
    private int progress;
    private static final String DEFAULT_PATTERN = "%d%%";

    FirebaseAuth firebaseAuth;
    private String uid;
    long exerciseTime;
    FirebaseUser loginUser;
    private FirebaseDatabase userDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference userReference = userDatabase.getReference();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();//get instance to firebaseAuth
        uid = firebaseAuth.getCurrentUser().getUid();

        loginUser = FirebaseAuth.getInstance().getCurrentUser();
        uid = loginUser != null ? loginUser.getUid() : null;

        DatabaseReference time = userReference.child("users").child(uid).child("record").child("total").child("time");
        time.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    exerciseTime = ds.getValue(Long.class);//아이템 획득 기능이 추가되면 거기 맞추어 타입 바꾸기
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        amount_calorie_consumption = view.findViewById(R.id.amount_calorie_consumption);
        normal_btn = (Button) view.findViewById(R.id.normal_exercise);
        recommend_btn = (Button) view.findViewById(R.id.recommend_exercise);
        circleProgressBar = view.findViewById(R.id.cpb_circlebar);

        //일반 운동 모드(Endless mode)
        normal_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(view.getContext(), ExerciseSplash.class);
                intent.addFlags (Intent.FLAG_ACTIVITY_NO_ANIMATION);
                view.getContext().startActivity(intent);

            }
        });

        //운동 추천 모드
        recommend_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(exerciseTime > 0)
                {
                    Intent intent = new Intent(view.getContext(), recActivity.class);
                    view.getContext().startActivity(intent);
                }
                else{
                    Toast.makeText(getContext(),"아직 기록된 운동이 없습니다. endless와 challange 모드를 통해 기록을 남기고 운동을 추천받아보세요!", Toast.LENGTH_LONG).show();
                }

            }
        });

        //사용자의 하루 운동 소모량
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users").child(uid);
        databaseReference.child("record").child("daily").child("calorie").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                calorie = (double) dataSnapshot.getValue(Double.class);
                //Record record = dataSnapshot.getValue(Record.class);
                amount_calorie_consumption.setText(calorie + "kcal");

                //원형 프로그래스 바 - 사용자의 목표 칼로리 대비 얼마나 많은 칼로리를 소모했느냐
                planCalorie = 66;
                progress = (int)( calorie / planCalorie * 100);
                if (progress > 100)
                    progress = 100;
                circleProgressBar.setProgress(progress);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("fragmentHome", String.valueOf(databaseError.toException())); // 에러문 출력
            }
        });

        // Inflate the layout for this fragment
        return view;
    }
    @Override
    public CharSequence format(int progress, int max) {
        return String.format(DEFAULT_PATTERN, (int) ((float) progress / (float) max * 100));
    }
}