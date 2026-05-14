package com.aksharadeep.tutor.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.aksharadeep.tutor.R;
import com.aksharadeep.tutor.database.AppDatabase;
import com.aksharadeep.tutor.models.Chapter;
import com.aksharadeep.tutor.models.Question;
import com.aksharadeep.tutor.models.QuizResult;

import java.util.List;

public class QuizActivity extends AppCompatActivity {

    public static final String EXTRA_CHAPTER_ID = "chapter_id";
    public static final String EXTRA_CHAPTER_NAME = "chapter_name";
    public static final String EXTRA_SUBJECT_ID = "subject_id";

    private AppDatabase db;
    private List<Question> questions;
    private int currentIndex = 0;
    private int score = 0;
    private String selectedAnswer = null;
    private boolean answered = false;
    private int chapterId, subjectId;
    private String chapterName;

    private TextView tvQuestionNum, tvQuestion, tvTimer, tvExplanation;
    private TextView[] optionViews;
    private Button btnNext, btnReview;
    private ProgressBar progressBar;
    private CountDownTimer countDownTimer;
    private LinearLayout layoutExplanation;

    private static final int TIMER_SECONDS = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        chapterId = getIntent().getIntExtra(EXTRA_CHAPTER_ID, -1);
        subjectId = getIntent().getIntExtra(EXTRA_SUBJECT_ID, -1);
        chapterName = getIntent().getStringExtra(EXTRA_CHAPTER_NAME);

        db = AppDatabase.getDatabase(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Quiz: " + chapterName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initViews();
        loadQuestions();
    }

    private void initViews() {
        tvQuestionNum = findViewById(R.id.tv_question_num);
        tvQuestion = findViewById(R.id.tv_question);
        tvTimer = findViewById(R.id.tv_timer);
        tvExplanation = findViewById(R.id.tv_explanation);
        layoutExplanation = findViewById(R.id.layout_explanation);
        progressBar = findViewById(R.id.progress_quiz);
        btnNext = findViewById(R.id.btn_next);
        btnReview = findViewById(R.id.btn_review);

        optionViews = new TextView[]{
                findViewById(R.id.option_a),
                findViewById(R.id.option_b),
                findViewById(R.id.option_c),
                findViewById(R.id.option_d)
        };

        String[] keys = {"A", "B", "C", "D"};
        for (int i = 0; i < optionViews.length; i++) {
            final String key = keys[i];
            optionViews[i].setOnClickListener(v -> selectAnswer(key));
        }

        btnNext.setOnClickListener(v -> {
            if (!answered) {
                Toast.makeText(this, "Please select an answer!", Toast.LENGTH_SHORT).show();
                return;
            }
            moveToNext();
        });

        btnReview.setOnClickListener(v -> toggleExplanation());
    }

    private void loadQuestions() {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            questions = db.questionDao().getQuestionsForQuiz(chapterId);
            runOnUiThread(() -> {
                if (questions.isEmpty()) {
                    Toast.makeText(this, "No questions available for this chapter yet.", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }
                showQuestion();
            });
        });
    }

    private void showQuestion() {
        if (currentIndex >= questions.size()) {
            finishQuiz();
            return;
        }

        answered = false;
        selectedAnswer = null;
        layoutExplanation.setVisibility(View.GONE);
        btnReview.setVisibility(View.GONE);

        Question q = questions.get(currentIndex);
        tvQuestionNum.setText("Question " + (currentIndex + 1) + " of " + questions.size());
        tvQuestion.setText(q.questionText);
        tvExplanation.setText("💡 " + q.explanation);

        String[] options = {q.optionA, q.optionB, q.optionC, q.optionD};
        String[] labels = {"A) ", "B) ", "C) ", "D) "};
        for (int i = 0; i < optionViews.length; i++) {
            optionViews[i].setText(labels[i] + options[i]);
            optionViews[i].setBackgroundResource(R.drawable.quiz_option_bg);
            optionViews[i].setTextColor(Color.parseColor("#212121"));
            optionViews[i].setEnabled(true);
        }

        progressBar.setMax(questions.size());
        progressBar.setProgress(currentIndex + 1);

        btnNext.setText(currentIndex == questions.size() - 1 ? "Submit Quiz" : "Next →");

        startTimer();
    }

    private void startTimer() {
        if (countDownTimer != null) countDownTimer.cancel();

        countDownTimer = new CountDownTimer(TIMER_SECONDS * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int secs = (int) (millisUntilFinished / 1000);
                tvTimer.setText("⏱ " + secs + "s");
                if (secs <= 10) {
                    tvTimer.setTextColor(Color.RED);
                } else {
                    tvTimer.setTextColor(Color.parseColor("#1A237E"));
                }
            }

            @Override
            public void onFinish() {
                tvTimer.setText("⏱ 0s");
                if (!answered) {
                    // Auto-mark as wrong
                    answered = true;
                    highlightAnswers();
                    btnReview.setVisibility(View.VISIBLE);
                    Toast.makeText(QuizActivity.this, "Time's up! ⏰", Toast.LENGTH_SHORT).show();
                }
            }
        }.start();
    }

    private void selectAnswer(String key) {
        if (answered) return;
        answered = true;
        selectedAnswer = key;

        Question q = questions.get(currentIndex);
        if (key.equals(q.correctAnswer)) score++;

        highlightAnswers();
        btnReview.setVisibility(View.VISIBLE);

        if (countDownTimer != null) countDownTimer.cancel();
    }

    private void highlightAnswers() {
        Question q = questions.get(currentIndex);
        String[] keys = {"A", "B", "C", "D"};

        for (int i = 0; i < optionViews.length; i++) {
            optionViews[i].setEnabled(false);
            String k = keys[i];
            if (k.equals(q.correctAnswer)) {
                optionViews[i].setBackgroundResource(R.drawable.quiz_correct_bg);
                optionViews[i].setTextColor(Color.parseColor("#1B5E20"));
            } else if (k.equals(selectedAnswer)) {
                optionViews[i].setBackgroundResource(R.drawable.quiz_wrong_bg);
                optionViews[i].setTextColor(Color.parseColor("#B71C1C"));
            }
        }
    }

    private void toggleExplanation() {
        if (layoutExplanation.getVisibility() == View.GONE) {
            layoutExplanation.setVisibility(View.VISIBLE);
            btnReview.setText("Hide Explanation");
        } else {
            layoutExplanation.setVisibility(View.GONE);
            btnReview.setText("Review Answer");
        }
    }

    private void moveToNext() {
        currentIndex++;
        if (currentIndex >= questions.size()) {
            finishQuiz();
        } else {
            showQuestion();
        }
    }

    private void finishQuiz() {
        if (countDownTimer != null) countDownTimer.cancel();

        int percent = (score * 100) / questions.size();

        // Save result to DB
        AppDatabase.databaseWriteExecutor.execute(() -> {
            db.quizResultDao().insert(new QuizResult(chapterId, subjectId, score, questions.size()));
            db.chapterDao().updateQuizScore(chapterId, percent);
        });

        // Navigate to Result screen
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra(ResultActivity.EXTRA_SCORE, score);
        intent.putExtra(ResultActivity.EXTRA_TOTAL, questions.size());
        intent.putExtra(ResultActivity.EXTRA_CHAPTER_NAME, chapterName);
        intent.putExtra(ResultActivity.EXTRA_CHAPTER_ID, chapterId);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
