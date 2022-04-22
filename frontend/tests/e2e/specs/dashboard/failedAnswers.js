describe('Student Walkthrough', () => {
    beforeEach( () => {
        cy.deleteQuestionsAndAnswers();
        // 1st sequence part - teacher creates quiz
        cy.demoTeacherLogin();
        cy.createQuestion(
            'Question Failed Answer 1',
            'Question',
            'Option',
            'Option',
            'ChooseThisWrong',
            'Correct'
        );
        cy.createQuestion(
            'Question Failed Answer 2',
            'Question',
            'Option',
            'Option',
            'ChooseThisWrong',
            'Correct'
        );
        cy.createQuizzWith2Questions(
            'Quiz Title',
            'Question Failed Answer 1',
            'Question Failed Answer 2'
        );
        cy.contains('Logout').click();
    });

    afterEach( () => {
        cy.deleteFailedAnswers();
        cy.deleteQuestionsAndAnswers();
    });

    it('student access its failed answers dashboard', () => {
        cy.demoStudentLogin();
        cy.solveQuizzWrong('Quiz Title', 2, "ChooseThisWrong");

        cy.accessFailedAnswerDashboard();
        cy.refreshFailedAnswers();
        cy.showStudentViewDialog();
        cy.deleteFailedAnswerFromDashboard();

        cy.contains('Logout').click();
        Cypress.on('uncaught:exception', (err, runnable) => {
            return false;
        });
    });
});