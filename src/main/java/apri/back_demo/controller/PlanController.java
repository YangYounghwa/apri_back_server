package apri.back_demo.controller;


// TODO List 
// 1. Fix login process. From session we need to get apri_id, maybe add apri_id in the UserSession.java  or add a column to the session table. (this willbefaster)
// 2. Change ALL Request and responses. 
//      - Sessions should be in the body.
//      - Responses in same format. (and edit the Exception handler as well)
//      - Finish this file.

import apri.back_demo.dto.*;
import apri.back_demo.dto.request.ApiRequest;
import apri.back_demo.dto.request.data.*;
import apri.back_demo.dto.response.ApiResponse;
import apri.back_demo.service.PlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plans")
public class PlanController {

    @Autowired
    private PlanService planService;

    /**
     * Creates a new total travel plan for a user.
     * POST /api/plans/create
     * Body: { "auth": { "apriId": 123 }, "data": { "title": "...", "startDate": "...", ... } }
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<TotalPlanDto>> createPlan(@RequestBody ApiRequest<CreatePlanData> request) {
        // --- Validation ---
        if (request == null || request.getAuth() == null || request.getAuth().getApriId() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Authentication information is missing."));
        }
        if (request.getData() == null || request.getData().getTitle() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Plan title is required."));
        }
        // --- End Validation ---

        Long apriId = request.getAuth().getApriId();
        CreatePlanData data = request.getData();

        TotalPlanDto planDto = new TotalPlanDto();
        planDto.setTitle(data.getTitle());
        planDto.setStartDate(data.getStartDate());
        planDto.setEndDate(data.getEndDate());
        planDto.setNotes(data.getNotes());

        TotalPlanDto createdPlan = planService.createTotalPlan(planDto, apriId);
        return new ResponseEntity<>(ApiResponse.success(createdPlan), HttpStatus.CREATED);
    }

    /**
     * Gets a list of all plan summaries for a specific user.
     * POST /api/plans/list
     * Body: { "auth": { "apriId": 123 }, "data": {} }
     */
    @PostMapping("/list")
    public ResponseEntity<ApiResponse<List<TotalPlanSummaryDto>>> getAllUserPlans(@RequestBody ApiRequest<Void> request) {
        if (request == null || request.getAuth() == null || request.getAuth().getApriId() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Authentication information is missing."));
        }
        Long apriId = request.getAuth().getApriId();
        List<TotalPlanSummaryDto> plans = planService.getAllPlansForUser(apriId);
        return ResponseEntity.ok(ApiResponse.success(plans));
    }

    /**
     * Gets a "simple" view of a specific plan.
     * POST /api/plans/get-simple
     * Body: { "auth": { "apriId": 123 }, "data": { "planId": 1 } }
     */
    @PostMapping("/get-simple")
    public ResponseEntity<ApiResponse<TotalPlanDto>> getSimplePlan(@RequestBody ApiRequest<PlanIdData> request) {
        if (request == null || request.getAuth() == null || request.getAuth().getApriId() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Authentication information is missing."));
        }
        if (request.getData() == null || request.getData().getPlanId() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("planId is required."));
        }
        
        Long apriId = request.getAuth().getApriId();
        Long planId = request.getData().getPlanId();

        TotalPlanDto plan = planService.getTotalPlanSimple(planId, apriId);
        if (plan != null) {
            return ResponseEntity.ok(ApiResponse.success(plan));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Plan not found or you do not have permission."));
        }
    }

    /**
     * Gets a "full" detailed view of a specific plan.
     * POST /api/plans/get-full
     * Body: { "auth": { "apriId": 123 }, "data": { "planId": 1 } }
     */
    @PostMapping("/get-full")
    public ResponseEntity<ApiResponse<TotalPlanDto>> getFullPlan(@RequestBody ApiRequest<PlanIdData> request) {
        if (request == null || request.getAuth() == null || request.getAuth().getApriId() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Authentication information is missing."));
        }
        if (request.getData() == null || request.getData().getPlanId() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("planId is required."));
        }

        Long apriId = request.getAuth().getApriId();
        Long planId = request.getData().getPlanId();

        TotalPlanDto plan = planService.getTotalPlanFull(planId, apriId);
        if (plan != null) {
            return ResponseEntity.ok(ApiResponse.success(plan));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Plan not found or you do not have permission."));
        }
    }

    /**
     * Updates the title and notes of a total plan.
     * POST /api/plans/update-details
     * Body: { "auth": { "apriId": 123 }, "data": { "planId": 1, "title": "...", "notes": "..." } }
     */
    @PostMapping("/update-details")
    public ResponseEntity<ApiResponse<String>> updatePlanDetails(@RequestBody ApiRequest<UpdatePlanDetailsData> request) {
         if (request == null || request.getAuth() == null || request.getAuth().getApriId() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Authentication information is missing."));
        }
        if (request.getData() == null || request.getData().getPlanId() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("planId is required."));
        }

        Long apriId = request.getAuth().getApriId();
        UpdatePlanDetailsData data = request.getData();

        boolean success = planService.updateTotalPlanDetails(data.getPlanId(), apriId, data.getTitle(), data.getNotes());
        if (success) {
            return ResponseEntity.ok(ApiResponse.success("Plan details updated successfully."));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Plan not found or you do not have permission to update."));
        }
    }

    /**
     * Updates all items for a specific daily plan. This is a full replacement.
     * POST /api/plans/update-daily-items
     * Body: { "auth": { "apriId": 123 }, "data": { "dailyPlanId": 10, "items": [...] } }
     */
    @PostMapping("/update-daily-items")
    public ResponseEntity<ApiResponse<String>> updateDailyPlan(@RequestBody ApiRequest<UpdateDailyPlanItemsData> request) {
        if (request == null || request.getAuth() == null || request.getAuth().getApriId() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Authentication information is missing."));
        }
        if (request.getData() == null || request.getData().getDailyPlanId() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("dailyPlanId is required."));
        }

        Long apriId = request.getAuth().getApriId();
        UpdateDailyPlanItemsData data = request.getData();
        
        // The items list can be null or empty if the user wants to clear the day
        List<PlanItemDto> items = data.getItems() != null ? data.getItems() : List.of();

        boolean success = planService.updateDailyPlanItems(data.getDailyPlanId(), items, apriId);
        if (success) {
            return ResponseEntity.ok(ApiResponse.success("Daily plan updated successfully."));
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.error("Daily plan not found or you do not have permission to update."));
        }
    }

    /**
     * Deletes a total plan and all its associated data.
     * POST /api/plans/delete
     * Body: { "auth": { "apriId": 123 }, "data": { "planId": 1 } }
     */
    @PostMapping("/delete")
    public ResponseEntity<ApiResponse<String>> deletePlan(@RequestBody ApiRequest<PlanIdData> request) {
        if (request == null || request.getAuth() == null || request.getAuth().getApriId() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Authentication information is missing."));
        }
        if (request.getData() == null || request.getData().getPlanId() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("planId is required."));
        }

        Long apriId = request.getAuth().getApriId();
        Long planId = request.getData().getPlanId();

        boolean success = planService.deleteTotalPlan(planId, apriId);
        if (success) {
            return ResponseEntity.ok(ApiResponse.success("Plan deleted successfully."));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error("Plan not found or you do not have permission to delete."));
        }
    }
}