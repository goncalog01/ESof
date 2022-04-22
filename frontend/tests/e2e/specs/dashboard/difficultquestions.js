describe('Student Difficult Question Dashboard Access', () => {
    beforeEach(() => {
        cy.deleteQuestionsAndAnswers();
        //create quiz
        cy.demoTeacherLogin();
        cy.createQuestion(
            'Question 1',
            'Which of these answers is "Correct"?',
            'correct',
            'Wrong',
            'Corrrrrrrrect',
            'Correct'
        );
        cy.createQuestion(
            'Question 2',
            'Which of these answers is "wrong"?',
            'Wrong',
            'Correct',
            'Corrrrrrrrect',
            'wrong'
        );
        cy.createQuizzWith2Questions(
            'Quiz Title',
            'Question 1',
            'Question 2'
        );
        cy.contains('Logout').click();
    });

    afterEach(() => {
        cy.deleteDifficultQuestions();
        cy.deleteQuestionsAndAnswers();
    })

    it('student answers quizz', () => {
        cy.demoStudentLogin();
        cy.solveQuizzWrong('Quiz Title', 2, 'Wrong');

        cy.accessDifficultQuestionsDashboard();

        cy.refreshDifficultQuestionsDashboard();
        cy.showDifficultQuestionsDashboard(2);
        cy.deleteDifficultQuestionsDashboard(2);

        cy.contains('Logout').click();
        Cypress.on('uncaught:exception', (err, runnable) => {
            // returning false here prevents Cypress from
            // failing the test
            return false;
        });
    });
});