package com.frostox.calculo.activities;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.frostox.calculo.Entities.McqItem;
import com.frostox.calculo.Nodes.MCQs;
import com.frostox.calculo.adapters.ResultData;
import com.frostox.calculo.adapters.Resultadapter;
import com.frostox.calculo.dao.DaoMaster;
import com.frostox.calculo.dao.DaoSession;
import com.frostox.calculo.dao.McqItemDao;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import calculo.frostox.com.calculo.R;
import io.github.kexanie.library.MathView;

public class McqActivity extends AppCompatActivity {


    TextView optionA, optionB, optionC, optionD, qn, questionnumber, click, score;
    ImageView imga, imgb, imgc, imgd, imgquestion;
    String id, namebar, difficulty, noq;
    CardView cardview;
    Button Skip;

    String[] ans, ansA, ansB, ansC, ansD, explanation, explanationType, name, question, topic, type, key;
    String[] rvqno, rvexpurl, rvurl, rvansurl, rvqn, rvans, rvexp;
    String[] rvqno2, rvexpurl2, rvurl2, rvansurl2, rvqn2, rvans2, rvexp2;
    int[] ct;
    LinearLayout.LayoutParams fill, empty;
    RelativeLayout choosea, chooseb, choosec, choosed, prntrl;

    int count, scorecount;
    int page;

    boolean[] checkanswer;

    boolean noqmode = false;

    RecyclerView rv;
    Resultadapter ra;
    Firebase ref;

    private List<McqItem> mcqItems;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                // Navigate "up" the demo structure to the launchpad activity.
                // See http://developer.android.com/design/patterns/navigation.html for more.
                NavUtils.navigateUpTo(this, new Intent(this, MainActivity.class));
                return true;


        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mcq);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        click = (TextView) findViewById(R.id.answer);
        questionnumber = (TextView) findViewById(R.id.questionnumber);
        imgquestion = (ImageView) findViewById(R.id.imgquestion);
        click.setVisibility(View.INVISIBLE);
        questionnumber.setVisibility(View.INVISIBLE);
        prntrl = (RelativeLayout) findViewById(R.id.prntrl);
        prntrl.setVisibility(View.INVISIBLE);

        rv = (RecyclerView) findViewById(R.id.rv);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        rv.setLayoutManager(llm);

        ref = new Firebase("https://extraclass.firebaseio.com/mcqs/");

        Intent intent = this.getIntent();
        id = intent.getStringExtra("id");
        difficulty = intent.getStringExtra("difficulty");
        noq = intent.getStringExtra("noq");

        namebar = "Default";
        Query query = ref.orderByChild("topic").equalTo(id);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                int length = (int) dataSnapshot.getChildrenCount();
                ans = new String[length];
                ansA = new String[length];
                ansB = new String[length];
                ansC = new String[length];
                ansD = new String[length];
                type = new String[length];
                name = new String[length];
                question = new String[length];
                explanation = new String[length];
                key = new String[length];
                count = 0;
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    key[count] = postSnapshot.getKey();
                    MCQs mcqtext = postSnapshot.getValue(MCQs.class);
                    if (difficulty.equals(Integer.toString(mcqtext.getDifficulty()))) {
                        ans[count] = mcqtext.getAns();
                        ansA[count] = mcqtext.getAnsA();
                        ansB[count] = mcqtext.getAnsB();
                        ansC[count] = mcqtext.getAnsC();
                        ansD[count] = mcqtext.getAnsD();
                        type[count] = mcqtext.getType();
                        name[count] = mcqtext.getName();
                        question[count] = mcqtext.getQuestion();
                        explanation[count] = mcqtext.getExplanation();
                        count++;
                    }
                }
                if (count != 0) {
                    initViews();
                } else {
                    click.setVisibility(View.VISIBLE);
                    click.setText("No MCQs over here");
                    Toast.makeText(McqActivity.this, "Sorry there are no MCQs", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                System.out.println("The read failed: " + firebaseError.getMessage());
            }
        });

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void initViews() {

        prntrl.setVisibility(View.VISIBLE);
        ct = new int[count];
        rvqn = new String[count];
        rvqno = new String[count];
        rvurl = new String[count];
        rvansurl = new String[count];
        rvexpurl = new String[count];
        rvans = new String[count];
        rvexp = new String[count];

        Skip = (Button) findViewById(R.id.skip);
        cardview = (CardView) findViewById(R.id.card_view);
        choosea = (RelativeLayout) findViewById(R.id.choosea);
        chooseb = (RelativeLayout) findViewById(R.id.chooseb);
        choosec = (RelativeLayout) findViewById(R.id.choosec);
        choosed = (RelativeLayout) findViewById(R.id.choosed);

        qn = (TextView) findViewById(R.id.question);
        optionA = (TextView) findViewById(R.id.ansa);
        optionB = (TextView) findViewById(R.id.ansb);
        optionC = (TextView) findViewById(R.id.ansc);
        optionD = (TextView) findViewById(R.id.ansd);
        score = (TextView) findViewById(R.id.score);
        scorecount = 0;
        click = (TextView) findViewById(R.id.answer);
        questionnumber = (TextView) findViewById(R.id.questionnumber);
        imga = (ImageView) findViewById(R.id.imga);
        imgb = (ImageView) findViewById(R.id.imgb);
        imgc = (ImageView) findViewById(R.id.imgc);
        imgd = (ImageView) findViewById(R.id.imgd);
        imgquestion = (ImageView) findViewById(R.id.imgquestion);
        click.setVisibility(View.VISIBLE);
        questionnumber.setVisibility(View.VISIBLE);
        qn.setVisibility(View.INVISIBLE);
        imgquestion.setVisibility(View.INVISIBLE);
        textinvisible();
        imginvisible();
        TouchListener(choosea, "A");
        TouchListener(chooseb, "B");
        TouchListener(choosec, "C");
        TouchListener(choosed, "D");
        page = 0;
        checkanswer = new boolean[count];
        load(0);
    }


    public void onClickPrev(View v) {
        --page;
        if (page >= (-1)) {
            load(page);
        } else {
            Toast.makeText(McqActivity.this, "No MCQs Behind", Toast.LENGTH_LONG).show();
        }

    }

    public void onClickNext(View v) {
        ++page;

        if (v.getId() == R.id.skip) {
            Toast.makeText(getBaseContext(),"Clicked here",Toast.LENGTH_LONG).show();
     /*       ct[page] = R.drawable.skip;
            load(page);
            ++page;
            load(page);*/
        }
        final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) this
                .findViewById(android.R.id.content)).getChildAt(0);

        if (v == null)
            Snackbar.make(viewGroup, "Correct!", Snackbar.LENGTH_LONG).show();

        if (v != null)
            Snackbar.make(viewGroup, "Wrong!", Snackbar.LENGTH_LONG).show();

        if (page == (Integer.parseInt(noq))) {
            noqmode = true;
            Toast.makeText(McqActivity.this, "All Done!", Toast.LENGTH_LONG).show();
            getSupportActionBar().setTitle("Result");
            textinvisible();
            imginvisible();
            prntrl.setVisibility(View.INVISIBLE);
            score.setVisibility(View.VISIBLE);
            cardview.setVisibility(View.VISIBLE);
            score.setText("Your score is: " + scorecount);
            rv.setVisibility(View.VISIBLE);
            ra = new Resultadapter(this, getdata());
            rv.setAdapter(ra);
        }

        if (page != count && count != 0) {
            load(page);
        } else if (page >= count) {
            Toast.makeText(McqActivity.this, "All Done!", Toast.LENGTH_LONG).show();
            getSupportActionBar().setTitle("Result");
            textinvisible();
            imginvisible();
            prntrl.setVisibility(View.INVISIBLE);
            score.setVisibility(View.VISIBLE);
            cardview.setVisibility(View.VISIBLE);
            score.setText("Your score is: " + scorecount);
            rv.setVisibility(View.VISIBLE);
            ra = new Resultadapter(this, getdata());
            rv.setAdapter(ra);
        }

    }

    public void load(int i) {

        if (type[i].equals("text")) {
            textvisible();
            imginvisible();
            qn.setText(question[i]);
            optionA.setText("(a) " + ansA[i]);
            optionB.setText("(b) " + ansB[i]);
            optionC.setText("(c) " + ansC[i]);
            optionD.setText("(d) " + ansD[i]);
            questionnumber.setText("Q." + (i + 1));
            rvqno[i] = ("Q." + (i + 1));
            rvqn[i] = question[i];
            rvurl[i] = "http://www.frostox.com/extraclass/uploads/";
            rvexpurl[i] = "http://www.frostox.com/extraclass/uploads/";
            if (ans[i].equals("A")) {
                rvans[i] = ansA[i];
            } else if (ans[i].equals("B")) {
                rvans[i] = ansB[i];
            } else if (ans[i].equals("C")) {
                rvans[i] = ansC[i];
            } else if (ans[i].equals("D")) {
                rvans[i] = ansD[i];
            }
            rvansurl[i] = ("http://www.frostox.com/extraclass/uploads/");
            rvexp[i] = explanation[i];

        } else if (type[i].equals("image")) {
            textinvisible();
            imgvisible();
            Picasso.with(getBaseContext()).load("http://www.frostox.com/extraclass/uploads/" + key[i] + "quest").into(imgquestion);
            Picasso.with(getBaseContext()).load("http://www.frostox.com/extraclass/uploads/" + key[i] + "A").into(imga);
            Picasso.with(getBaseContext()).load("http://www.frostox.com/extraclass/uploads/" + key[i] + "B").into(imgb);
            Picasso.with(getBaseContext()).load("http://www.frostox.com/extraclass/uploads/" + key[i] + "C").into(imgc);
            Picasso.with(getBaseContext()).load("http://www.frostox.com/extraclass/uploads/" + key[i] + "D").into(imgd);
            questionnumber.setText("Q." + (i + 1));
            rvqno[i] = ("Q." + (i + 1));
            rvqn[i] = "";
            rvurl[i] = ("http://www.frostox.com/extraclass/uploads/" + key[i] + "quest");
            rvexpurl[i] = ("http://www.frostox.com/extraclass/uploads/" + key[i] + "explanation");
            if (ans[i].equals("A")) {
                rvansurl[i] = ("http://www.frostox.com/extraclass/uploads/" + key[i] + "A");
            } else if (ans[i].equals("B")) {
                rvansurl[i] = ("http://www.frostox.com/extraclass/uploads/" + key[i] + "B");
            } else if (ans[i].equals("C")) {
                rvansurl[i] = ("http://www.frostox.com/extraclass/uploads/" + key[i] + "C");
            } else if (ans[i].equals("D")) {
                rvansurl[i] = ("http://www.frostox.com/extraclass/uploads/" + key[i] + "D");
            }
            rvans[i] = "";
            rvexp[i] = "";
        }
        getSupportActionBar().setTitle(name[i]);
    }

    public void vibrateDevice() {
        Vibrator v = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(500);

        final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) this
                .findViewById(android.R.id.content)).getChildAt(0);
        Snackbar.make(viewGroup, "Wrong!", Snackbar.LENGTH_LONG).show();
    }

    public void TouchListener(View v, final String option) {

        final String check = String.valueOf(v.getId());
        v.setOnTouchListener(new View.OnTouchListener() {
            public final static int FINGER_RELEASED = 0;
            public final static int FINGER_TOUCHED = 1;
            public final static int FINGER_DRAGGING = 2;
            public final static int FINGER_UNDEFINED = 3;
            private int fingerState = FINGER_RELEASED;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {


                switch (motionEvent.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        if (fingerState == FINGER_RELEASED) fingerState = FINGER_TOUCHED;
                        else fingerState = FINGER_UNDEFINED;

                        if (ans != null) {
                            String get = String.valueOf(view.getId());
                            Log.d("nuontouch", get);
                            if (ans[page].equals(option)) {
                                ct[page] = R.drawable.mark;
                                checkanswer[page] = true;
                                scorecount++;
                                onClickNext(null);
                            } else {
                                ct[page] = R.drawable.cross;
                                checkanswer[page] = false;
                                onClickNext((View) findViewById(R.id.dummy));
                                vibrateDevice();
                            }
                        }

                        break;

                    case MotionEvent.ACTION_UP:
                        if (fingerState != FINGER_DRAGGING) {
                            fingerState = FINGER_RELEASED;

                        } else if (fingerState == FINGER_DRAGGING) fingerState = FINGER_RELEASED;
                        else fingerState = FINGER_UNDEFINED;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        if (fingerState == FINGER_TOUCHED || fingerState == FINGER_DRAGGING)
                            fingerState = FINGER_DRAGGING;
                        else fingerState = FINGER_UNDEFINED;
                        break;

                    default:
                        fingerState = FINGER_UNDEFINED;

                }

                return false;
            }
        });

    }

    public List<ResultData> getdata() {
        List<ResultData> input = new ArrayList<>();
        int length;
        if (noqmode) length = (Integer.parseInt(noq));
        else length = count;
        for (int i = 0; i < length; i++) {
            ResultData current = new ResultData(rvurl[i], rvansurl[i], rvexpurl[i], rvqno[i], rvqn[i], rvans[i], rvexp[i], ct[i]);
            current.ct = ct[i];
            current.imgansurl = rvansurl[i];
            current.imgqnurl = rvurl[i];
            current.expurl = rvexpurl[i];
            current.qno = (rvqno[i]);
            current.qn = rvqn[i];
            current.ans = rvans[i];
            current.exp = rvexp[i];
            input.add(current);
        }
        return input;
    }

    public void textvisible() {
        qn.setVisibility(View.VISIBLE);
        optionA.setVisibility(View.VISIBLE);
        optionB.setVisibility(View.VISIBLE);
        optionC.setVisibility(View.VISIBLE);
        optionD.setVisibility(View.VISIBLE);
    }

    public void textinvisible() {
        qn.setVisibility(View.INVISIBLE);
        optionA.setVisibility(View.INVISIBLE);
        optionB.setVisibility(View.INVISIBLE);
        optionC.setVisibility(View.INVISIBLE);
        optionD.setVisibility(View.INVISIBLE);
    }

    public void imgvisible() {
        imgquestion.setVisibility(View.VISIBLE);
        imga.setVisibility(View.VISIBLE);
        imgb.setVisibility(View.VISIBLE);
        imgc.setVisibility(View.VISIBLE);
        imgd.setVisibility(View.VISIBLE);
    }

    public void imginvisible() {
        imgquestion.setVisibility(View.INVISIBLE);
        imga.setVisibility(View.INVISIBLE);
        imgb.setVisibility(View.INVISIBLE);
        imgc.setVisibility(View.INVISIBLE);
        imgd.setVisibility(View.INVISIBLE);

    }
}

