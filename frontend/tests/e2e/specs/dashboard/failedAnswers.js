describe('Student Walkthrough', () => {
    beforeEach( () => {
        cy.deleteQuestionsAndAnswers();
        // 1st sequence part - teacher creates quiz
        cy.demoTeacherLogin();
        cy.createQuestion(
            'Question Title',
            'Question',
            'Option',
            'Option',
            'ChooseThisWrong',
            'Correct'
        );
        cy.createQuestion(
            'Question Title2',
            'Question',
            'ChooseThisWrong',
            'Option',
            'Option',
            'Correct'
        );
        cy.createQuizzWith2Questions(
            'Quiz Title',
            'Question Title',
            'Question Title2'
        );
        cy.contains('Logout').click();
    });

    afterEach( () => {
        cy.deleteFailedAnswers();
        cy.deleteQuestionsAndAnswers();
    });

    it('student access his failed answers dashboard', () => {
        cy.demoStudentLogin();
        cy.solveQuizzWrong('Quiz Title', 2, "ChooseThisWrong");

        cy.accessFailedAnswerDashboard();

        cy.refreshFailedAnswers();
        cy.showStudentViewDialog();
        cy.deleteFailedAnswerFromDashboardError(0);

        cy.refreshFailedAnswers();
        cy.setFailedAnswersAsOld();
        cy.deleteFailedAnswerFromDashboard(0);


        cy.contains('Logout').click();
        Cypress.on('uncaught:exception', (err, runnable) => {
            // returning false here prevents Cypress from
            // failing the test
            return false;
        });
    });
});